package fr.openent.lool.helper;

import fr.openent.lool.core.constants.Field;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class PromiseHelper {
    /**
     * Returns promise handler. It used to manage promise callback
     *
     * @param promise Promise
     * @return Message handler
     */
    public static Handler<Message<JsonObject>> getPromiseHandler(Promise<JsonObject> promise) {
        return event -> {
            if (Field.OK.equals(event.body().getString(Field.STATUS))) {
                promise.complete(event.body());
            } else {
                promise.fail(event.body().getString("message"));
            }
        };
    }
}
