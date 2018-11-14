package fr.openent.lool.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public interface DocumentService {

    /**
     * Get document from workspace
     *
     * @param documentId Document id
     * @param handler    Function handler returning data
     */
    void get(String documentId, Handler<Either<String, JsonObject>> handler);
}
