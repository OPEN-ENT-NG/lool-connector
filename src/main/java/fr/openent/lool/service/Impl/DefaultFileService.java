package fr.openent.lool.service.Impl;

import fr.openent.lool.service.FileService;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import org.entcore.common.storage.Storage;

public class DefaultFileService implements FileService {

    private Storage storage;

    public DefaultFileService(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void get(String fileId, Handler<Buffer> handler) {
        storage.readFile(fileId, new Handler<Buffer>() {
            @Override
            public void handle(Buffer buffer) {
                handler.handle(buffer);
            }
        });
    }
}
