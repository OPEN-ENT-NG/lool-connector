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

    /**
     * Update file in workspace
     *
     * @param documentId document id to update
     * @param newFileId  new file id
     * @param metadata   new file metadata
     * @param handler    Function handler returning data
     */
    void update(String documentId, String newFileId, JsonObject metadata, Handler<Either<String, JsonObject>> handler);

    /**
     * Update document revision identifier. It does not create new revision
     *
     * @param documentId Document identifier
     * @param newFileId  New file identifier
     * @param handler    Function handler returning data
     */
    void updateRevisionId(String documentId, String newFileId, Handler<Either<String, JsonObject>> handler);
}
