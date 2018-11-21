package fr.openent.lool.service;

import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

public interface FileService {

    /**
     * Get file based on provided Id
     *
     * @param fileId  File id
     * @param handler Function handler returning data
     */
    void get(String fileId, Handler<Buffer> handler);

    /**
     * Add file in file system
     *
     * @param request     Server request uploading file
     * @param contentType Content type file
     * @param filename    Filename
     * @param handler     Function handler returning data
     */
    void add(HttpServerRequest request, String contentType, String filename, Handler<Either<String, JsonObject>> handler);
}
