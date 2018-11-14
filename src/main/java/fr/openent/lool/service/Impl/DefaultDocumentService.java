package fr.openent.lool.service.Impl;

import fr.openent.lool.service.DocumentService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

public class DefaultDocumentService implements DocumentService {
    EventBus eb;

    public DefaultDocumentService(EventBus eb) {
        this.eb = eb;
    }

    @Override
    public void get(String documentId, Handler<Either<String, JsonObject>> handler) {
        JsonObject action = new JsonObject()
                .put("action", "getDocument")
                .put("id", documentId);
        eb.send("org.entcore.workspace", action, handlerToAsyncHandler(new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                JsonObject body = message.body();
                if (!"ok".equals(body.getString("status"))) {
                    handler.handle(new Either.Left<>("An error occurred when calling document by event bus"));
                } else {
                    handler.handle(new Either.Right<>(message.body().getJsonObject("result")));
                }
            }
        }));
    }
}
