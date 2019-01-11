package fr.openent.lool.service.Impl;

import fr.openent.lool.service.FileService;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.storage.Storage;

public class DefaultFileService implements FileService {

    private final Storage storage;

    public DefaultFileService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void get(String fileId, Handler<Buffer> handler) {
        storage.readFile(fileId, handler);
    }

    @Override
    public void add(HttpServerRequest request, String contentType, String filename, Handler<Either<String, JsonObject>> handler) {
        Buffer responseBuffer = new BufferImpl();
        request.handler(responseBuffer::appendBuffer);
        request.endHandler(aVoid -> storage.writeBuffer(responseBuffer, contentType, filename, entries -> {
            if ("ok".equals(entries.getString("status"))) {
                handler.handle(new Either.Right<>(entries));
            } else {
                handler.handle(new Either.Left<>("[DefaultFileService@add] An error occurred while writing file in the storage"));
            }
        }));
        request.exceptionHandler(throwable -> handler.handle(new Either.Left<>("[DefaultFileService@add]An error occurred when uploading file")));
    }

}
