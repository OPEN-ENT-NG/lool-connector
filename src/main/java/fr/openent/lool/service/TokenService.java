package fr.openent.lool.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface TokenService {
    /**
     * Create provisional token based on given document identifier
     *
     * @param documentId document identifier
     * @param userId     User identifier
     * @param handler    Function handler returning data
     */
    void create(String documentId, String userId, Handler<Either<String, JsonObject>> handler);

    /**
     * Get provisional document token
     *
     * @param id      Token identifier
     * @param handler Function handler returning data
     */
    void get(String id, Handler<Either<String, JsonObject>> handler);

    /**
     * Delete given provisional token
     *
     * @param id      Token identifier
     * @param handler Function handler returning data
     */
    void delete(String id, Handler<Either<String, JsonObject>> handler);
}
