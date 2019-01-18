package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.bean.Token;
import fr.openent.lool.service.DocumentService;
import fr.openent.lool.service.FileService;
import fr.openent.lool.service.Impl.DefaultDocumentService;
import fr.openent.lool.service.Impl.DefaultFileService;
import fr.openent.lool.utils.Bindings;
import fr.wseduc.rs.Delete;
import fr.wseduc.rs.Get;
import fr.wseduc.rs.Post;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserUtils;

import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class WopiController extends ControllerHelper {

    private final DocumentService documentService;
    private final FileService fileService;

    public WopiController(EventBus eb, Storage storage) {
        super();
        documentService = new DefaultDocumentService(eb, storage);
        fileService = new DefaultFileService(storage);
    }

    @Get("/wopi/files/:id")
    public void checkFileInfo(HttpServerRequest request) {
        String loolToken = request.params().get("access_token");
        String documentId = request.params().get("id");
        Lool.wopiHelper.validateToken(loolToken, documentId, Bindings.READ.toString(), validationObject -> {
            if (!validationObject.getBoolean("valid")) {
                unauthorized(request);
                return;
            }
            Token token = new Token(validationObject.getJsonObject("token"));
            Lool.wopiHelper.userCanWrite(token.getSessionId(), token.getDocument(), canWrite -> documentService.get(request.getParam("id"), event -> {
                if (event.isRight()) {
                    JsonObject document = event.right().getValue();
                    JsonObject metadata = document.getJsonObject("metadata");
                    //TODO Update options and get version revision from document
                    JsonObject response = new JsonObject()
                            .put("BaseFileName", document.getString("name"))
                            .put("Size", metadata.getInteger("size"))
                            .put("OwnerId", document.getString("owner"))
                            .put("UserId", token.getUser())
                            .put("UserFriendlyName", token.getDisplayName())
                            .put("Version", 34)
                            .put("DisableCopy", false)
                            .put("DisablePrint", false)
                            .put("DisableExport", false)
                            .put("HideExportOption", false)
                            .put("DisableInactiveMessages", false)
                            .put("HideUserList", false)
                            .put("HideSaveOption", false)
                            .put("EnableShare", true)
                            .put("EnableInsertRemoteImage", true)
                            .put("HidePrintOption", false)
                            .put("UserCanNotWriteRelative", false)
                            .put("UserCanWrite", canWrite);

                    renderJson(request, response);
                } else {
                    badRequest(request);
                }
            }));
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

    @Post("/wopi/files/:id/contents")
    public void putFile(HttpServerRequest request) {
        request.pause();
        boolean isAutoSave = Boolean.parseBoolean(request.getHeader("X-LOOL-WOPI-IsAutosave"));
        Lool.wopiHelper.validateToken(request.params().get("access_token"), request.params().get("id"), Bindings.CONTRIB.toString(), validation -> {
            if (!validation.getBoolean("valid")) {
                unauthorized(request);
                return;
            }

            documentService.get(request.getParam("id"), event -> {
                if (event.isRight()) {
                    JsonObject document = event.right().getValue();
                    JsonObject metadata = document.getJsonObject("metadata");
                    request.resume();
                    fileService.add(request, metadata.getString("content-type"), document.getString("name"), storageEvent -> {
                        if (storageEvent.isLeft()) {
                            log.error(storageEvent.left().getValue());
                            renderError(request);
                            return;
                        }
                        JsonObject storageBody = storageEvent.right().getValue();
                        Handler<Either<String, JsonObject>> updateHandler = updateEvent -> {
                            if (updateEvent.isRight()) {
                                request.response().setStatusCode(200).end();
                            } else {
                                renderError(request);
                            }
                        };

                        if (isAutoSave) {
                            documentService.updateRevisionId(request.getParam("id"), storageBody.getString("_id"), updateHandler);
                        } else {
                            documentService.update(request.getParam("id"), storageBody.getString("_id"), storageBody.getJsonObject("metadata"), updateHandler);
                        }
                    });
                } else {
                    renderError(request);
                }
            });
        });
    }

    @Delete("/wopi/documents/:id/tokens/:token")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void deleteToken(HttpServerRequest request) {
        String documentId = request.getParam("id");
        String accessToken = request.getParam("token");
        UserUtils.getUserInfos(eb, request, user -> {
            Lool.wopiHelper.isUserToken(user.getUserId(), accessToken, documentId, isUserToken -> {
                if (isUserToken) {
                    Lool.wopiHelper.deleteToken(accessToken, defaultResponseHandler(request));
                } else {
                    unauthorized(request);
                }
            });
        });
    }
}
