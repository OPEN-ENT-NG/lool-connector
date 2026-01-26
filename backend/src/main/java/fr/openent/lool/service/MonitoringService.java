package fr.openent.lool.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public interface MonitoringService {
    /**
     * Get current opened documents
     *
     * @param handler Function handler returning data
     */
    void getDocuments(Handler<Either<String, JsonArray>> handler);

    /**
     * Count connected users
     *
     * @param handler Function handler returning data
     */
    void countUsers(Handler<Either<String, JsonObject>> handler);

    /**
     * Count given evet
     *
     * @param event   Event name
     * @param handler Function handler returning data
     */
    void countEvent(String event, Handler<Either<String, JsonObject>> handler);


    /**
     * Get extensions grouped with count
     *
     * @param handler Function handler returning data
     */
    void getExtensions(Handler<Either<String, JsonArray>> handler);
}
