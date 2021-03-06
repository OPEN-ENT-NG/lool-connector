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

    /**
     * Add file in file system from http request
     *
     * @param request Http request uploading file
     * @param handler Function handler returning data
     */
    void add(HttpServerRequest request, Handler<Either<String, JsonObject>> handler);

    /**
     * Add file in file systeme based on given buffer
     *
     * @param file        File buffer
     * @param contentType File content type
     * @param filename    Filename
     * @param handler     Function handler returning data
     */
    void add(Buffer file, String contentType, String filename, Handler<Either<String, JsonObject>> handler);
}
