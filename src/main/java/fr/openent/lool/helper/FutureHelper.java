package fr.openent.lool.helper;

import fr.openent.lool.core.constants.Field;
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
            if (Field.OK.equals(event.body().getString(Field.STATUS))) {
                future.complete(event.body());
            } else {
                future.fail(event.body().getString("message"));
            }
        };
    }
}
