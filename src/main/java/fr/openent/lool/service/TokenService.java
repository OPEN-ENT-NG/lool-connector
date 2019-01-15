package fr.openent.lool.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface TokenService {

    /**
     * Get token based on token id and document id
     *
     * @param token    Lool token identifier
     * @param document document identifier
     * @param handler  Function handler returning data
     */
    void get(String token, String document, Handler<Either<String, JsonObject>> handler);

    /**
     * Clear token collection
     *
     * @param handler Function handler returning data
     */
    void clear(Handler<Either<String, JsonObject>> handler);
}
