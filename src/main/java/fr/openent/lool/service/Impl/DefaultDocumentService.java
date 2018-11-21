package fr.openent.lool.service.Impl;

import fr.openent.lool.service.DocumentService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

public class DefaultDocumentService implements DocumentService {
    EventBus eb;
    private String WORKSPACE_BUS_ADDRESS = "org.entcore.workspace";

    public DefaultDocumentService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void get(String documentId, Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .put("action", "getDocument")
                .put("id", documentId);
        eb.send(WORKSPACE_BUS_ADDRESS, action, handlerToAsyncHandler(message -> {
            JsonObject body = message.body();
            if (!"ok".equals(body.getString("status"))) {
                handler.handle(new Either.Left<>("An error occurred when calling document by event bus"));
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
                handler.handle(new Either.Left<>("An error occurred when calling document by event bus"));
            } else {
                handler.handle(new Either.Right<>(message.body().getJsonObject("result")));
            }
        }));
    }
}
