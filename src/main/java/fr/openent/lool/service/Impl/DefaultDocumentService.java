package fr.openent.lool.service.Impl;

import fr.openent.lool.helper.FutureHelper;
import fr.openent.lool.service.DocumentService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.storage.Storage;

import java.util.Date;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

public class DefaultDocumentService implements DocumentService {
    private final EventBus eb;
    private final Storage storage;
    private final String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";
    Logger log = LoggerFactory.getLogger(DefaultDocumentService.class);

    public DefaultDocumentService(EventBus eb, Storage storage) {
        this.eb = eb;
        this.storage = storage;
    }

    @Override
    public void get(String documentId, Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .put("action", "getDocument")
                .put("id", documentId);
        eb.send(WORKSPACE_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
            JsonObject body = message.body();
            if (!"ok".equals(body.getString("status"))) {
                handler.handle(new Either.Left<>("[DefaultDocumentService@get] An error occurred when calling document by event bus"));
            } else {
                handler.handle(new Either.Right<>(message.body().getJsonObject("result")));
            }
        }));
    }

    @Override
    public void update(String documentId, String newFileId, JsonObject metadata, Handler<Either<String, JsonObject>> handler) {
        JsonObject uploaded = new JsonObject()
                .put("_id", newFileId)
                .put("metadata", metadata);
        JsonObject action = new JsonObject()
                .put("action", "updateDocument")
                .put("id", documentId)
                .put("uploaded", uploaded);

        eb.send(WORKSPACE_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
            if (!"ok".equals(message.body().getString("status"))) {
                handler.handle(new Either.Left<>("[DefaultDocumentService@update]  An error occurred when calling document by event bus"));
            } else {
                handler.handle(new Either.Right<>(message.body().getJsonObject("result")));
            }
        }));
    }

    @Override
    public void updateRevisionId(String documentId, String newFileId, Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject().put("_id", documentId);
        JsonObject updater = new JsonObject().put("file", newFileId);
        JsonObject updaterRevision = updater.put("date", MongoDb.now());

        JsonObject documentMatcher = new JsonObject().put("documentId", documentId);
        JsonObject sort = new JsonObject().put("date", -1);
        JsonObject projection = new JsonObject().put("_id", 1).put("file", 1);

        Future<JsonObject> documentFuture = Future.future();
        Future<JsonObject> revisionFuture = Future.future();

        MongoDb.getInstance().find("documentsRevisions", documentMatcher, sort, projection, -1, 1,
                Integer.MAX_VALUE, MongoDbResult.validResultsHandler(either -> {
            if (either.isRight()) {
                JsonArray documents = either.right().getValue();
                if (documents.size() == 0) {
                    revisionFuture.fail("No revision found");
                    return;
                }
                JsonObject revisionMatcher = new JsonObject().put("_id", documents.getJsonObject(0).getString("_id"));
                MongoDb.getInstance().update("documentsRevisions", revisionMatcher, new JsonObject().put("$set", updaterRevision), event -> {
                    if ("ok".equals(event.body().getString("status"))) {
                        revisionFuture.complete(event.body());
                        storage.removeFile(documents.getJsonObject(0).getString("file"), entries -> {
                            if (!"ok".equals(entries.getString("status"))) {
                                log.error("[DefaultDocumentService@updateRevisionId] Failed to delete revision " + documents.getJsonObject(0).getString("file"));
                            }
                        });
                    } else {
                        revisionFuture.fail(event.body().getString("message"));
                    }
                });
            } else {
                revisionFuture.fail(either.left().getValue());
            }
        }));

        JsonObject updaterDocument = updater.put("modified", MongoDb.formatDate(new Date()));

        MongoDb.getInstance().update("documents", matcher, new JsonObject().put("$set", updaterDocument), FutureHelper.getFutureHandler(documentFuture));

        CompositeFuture.all(documentFuture, revisionFuture).setHandler(response -> {
            Either<String, JsonObject> ok = new Either.Right<>(new JsonObject().put("status", "ok"));
            Either<String, JsonObject> ko = new Either.Left<>("Failed to update revision and document");

            handler.handle(response.succeeded() ? ok : ko);
        });
    }
}
