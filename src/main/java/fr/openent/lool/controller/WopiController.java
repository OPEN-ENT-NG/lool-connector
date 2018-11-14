package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.service.DocumentService;
import fr.openent.lool.service.FileService;
import fr.openent.lool.service.Impl.DefaultDocumentService;
import fr.openent.lool.service.Impl.DefaultFileService;
import fr.wseduc.rs.Get;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.storage.Storage;

public class WopiController extends ControllerHelper {

    private DocumentService documentService;
    private FileService fileService;

    public WopiController(EventBus eb, Storage storage) {
        super();
        documentService = new DefaultDocumentService(eb);
        fileService = new DefaultFileService(storage);
    }

    @Get("/wopi/files/:id")
    public void checkFileInfo(HttpServerRequest request) {
        documentService.get(request.getParam("id"), event -> {
            if (event.isRight()) {
                JsonObject document = event.right().getValue();
                JsonObject metadata = document.getJsonObject("metadata");
                JsonObject response = new JsonObject()
                        .put("BaseFileName", document.getString("name"))
                        .put("Size", metadata.getInteger("size"))
                        .put("OwnerId", document.getString("owner"))
                        .put("UserId", LoolController.userWopiToken)
                        .put("UserFriendlyName", "Simon LEDUNOIS")
                        .put("Version", 34)
                        .put("DisableCopy", false)
                        .put("DisablePrint", false)
                        .put("DisableExport", false)
                        .put("HideExportOption", false)
                        .put("HideSaveOption", false)
                        .put("HidePrintOption", Lool.wopiHelper.getServer())
                        .put("UserCanWrite", true);

                renderJson(request, response);
            } else {
                badRequest(request);
            }
        });
    }

    @Get("/wopi/files/:id/contents")
    public void getFile(HttpServerRequest request) {
        documentService.get(request.getParam("id"), event -> {
            if (event.isRight()) {
                JsonObject document = event.right().getValue();
                fileService.get(document.getString("file"), buffer ->
                        request.response()
                                .setStatusCode(200)
                                .putHeader("Content-Type", "application/octet-stream")
                                .putHeader("Content-Transfer-Encoding", "Binary")
                                .putHeader("Content-disposition", "attachment; filename=" + document.getString("name"))
                                .end(buffer));
            } else {
                badRequest(request);
            }
        });
    }
}
