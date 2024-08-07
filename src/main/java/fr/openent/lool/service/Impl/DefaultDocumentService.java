package fr.openent.lool.service.Impl;

import fr.openent.lool.core.constants.Field;
import fr.openent.lool.helper.PromiseHelper;
import fr.openent.lool.service.DocumentService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
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
                .put(Field.ID, documentId);
        eb.request(WORKSPACE_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
            JsonObject body = message.body();
            if (!Field.OK.equals(body.getString(Field.STATUS))) {
                handler.handle(new Either.Left<>("[DefaultDocumentService@get] An error occurred when calling document by event bus"));
            } else {
                handler.handle(new Either.Right<>(message.body().getJsonObject("result")));
            }
        }));
    }

    @Override
    public void update(String documentId, String newFileId, JsonObject metadata, Handler<Either<String, JsonObject>> handler) {
        JsonObject uploaded = new JsonObject()
                .put(Field._ID, newFileId)
                .put(Field.METADATA, metadata);
        JsonObject action = new JsonObject()
                .put("action", "updateDocument")
                .put(Field.ID, documentId)
                .put("uploaded", uploaded);

        eb.request(WORKSPACE_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
            if (!Field.OK.equals(message.body().getString(Field.STATUS))) {
                handler.handle(new Either.Left<>("[DefaultDocumentService@update]  An error occurred when calling document by event bus"));
            } else {
                handler.handle(new Either.Right<>(message.body().getJsonObject("result")));
            }
        }));
    }

    @Override
    public void updateRevisionId(String documentId, String newFileId, Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject().put(Field._ID, documentId);
        JsonObject updater = new JsonObject().put(Field.FILE, newFileId);
        JsonObject updaterRevision = updater.put(Field.DATE, MongoDb.now());

        JsonObject documentMatcher = new JsonObject().put(Field.DOCUMENTID, documentId);
        JsonObject sort = new JsonObject().put(Field.DATE, -1);
        JsonObject projection = new JsonObject().put(Field._ID, 1).put(Field.FILE, 1);

        Promise<JsonObject> documentPromise = Promise.promise();
        Promise<JsonObject> revisionPromise = Promise.promise();

        MongoDb.getInstance().find(Field.DOCUMENTSREVISION, documentMatcher, sort, projection, -1, 1,
                Integer.MAX_VALUE, MongoDbResult.validResultsHandler(either -> {
            if (either.isRight()) {
                JsonArray documents = either.right().getValue();
                if (documents.size() == 0) {
                    revisionPromise.fail("No revision found");
                    return;
                }
                JsonObject revisionMatcher = new JsonObject().put(Field._ID, documents.getJsonObject(0).getString(Field._ID));
                MongoDb.getInstance().update(Field.DOCUMENTSREVISION, revisionMatcher, new JsonObject().put(Field.$SET, updaterRevision), event -> {
                    if (Field.OK.equals(event.body().getString(Field.STATUS))) {
                        revisionPromise.complete(event.body());
                        storage.removeFile(documents.getJsonObject(0).getString(Field.FILE), entries -> {
                            if (!Field.OK.equals(entries.getString(Field.STATUS))) {
                                log.error("[LOOL@DefaultDocumentService::updateRevisionId] Failed to delete revision : " + documents.getJsonObject(0).getString(Field.FILE));
                            }
                        });
                    } else {
                        revisionPromise.fail(event.body().getString("message"));
                    }
                });
            } else {
                revisionPromise.fail(either.left().getValue());
            }
        }));

        JsonObject updaterDocument = updater.put(Field.MODIFIED, MongoDb.formatDate(new Date()));

        MongoDb.getInstance().update(Field.DOCUMENTS, matcher, new JsonObject().put(Field.$SET, updaterDocument), PromiseHelper.getPromiseHandler(documentPromise));

        Future.all(documentPromise.future(), revisionPromise.future()).onComplete(response -> {
            Either<String, JsonObject> ok = new Either.Right<>(new JsonObject().put(Field.STATUS, Field.OK));
            Either<String, JsonObject> ko = new Either.Left<>("Failed to update revision and document");

            handler.handle(response.succeeded() ? ok : ko);
        });
    }
}
