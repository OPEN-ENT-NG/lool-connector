package fr.openent.lool.helper;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class FutureHelper {
    /**
     * Returns future handler. It used to manage future callback
     *
     * @param future Future
     * @return Message handler
     */
    public static Handler<Message<JsonObject>> getFutureHandler(Future<JsonObject> future) {
        return event -> {
            if ("ok".equals(event.body().getString("status"))) {
                future.complete(event.body());
            } else {
                future.fail(event.body().getString("message"));
            }
        };
    }
}
