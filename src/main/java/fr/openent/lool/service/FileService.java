package fr.openent.lool.service;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

public interface FileService {

    /**
     * Get file based on provided Id
     *
     * @param fileId  File id
     * @param handler Function handler returning data
     */
    void get(String fileId, Handler<Buffer> handler);
}
