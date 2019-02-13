package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.bean.Token;
import fr.openent.lool.helper.HttpHelper;
import fr.openent.lool.helper.TraceHelper;
import fr.openent.lool.service.DocumentService;
import fr.openent.lool.service.FileService;
import fr.openent.lool.service.Impl.DefaultDocumentService;
import fr.openent.lool.service.Impl.DefaultFileService;
import fr.openent.lool.service.Impl.DefaultTokenService;
import fr.openent.lool.service.TokenService;
import fr.openent.lool.utils.Actions;
import fr.openent.lool.utils.Bindings;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.data.FileResolver;
import fr.wseduc.webutils.request.CookieHelper;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class LoolController extends ControllerHelper {

    private final DocumentService documentService;
    private final FileService fileService;
    private final TokenService tokenService;
    private final EventStore eventStore;
    private final HttpHelper httpHelper;
    private WorkspaceHelper workspaceHelper;

    public LoolController(Vertx vertx, EventBus eb, Storage storage) {
        super();
        documentService = new DefaultDocumentService(eb, storage);
        fileService = new DefaultFileService(storage);
        tokenService = new DefaultTokenService();
        eventStore = EventStoreFactory.getFactory().getEventStore(Lool.class.getSimpleName());
        this.httpHelper = new HttpHelper(vertx);
        this.workspaceHelper = new WorkspaceHelper(eb, storage);
    }

    @Get("/documents/:id/open")
    @ApiDoc("Open document in Libre Office Online")
    @SecuredAction("open.file")
    public void open(HttpServerRequest request) {
        UserUtils.getUserInfos(eb, request, user -> {
            if (user == null) {
                unauthorized(request);
                return;
            }
            Lool.wopiHelper.generateLoolToken(request, tokenEvent -> {
                if (tokenEvent.isRight()) {
                    Token token = tokenEvent.right().getValue();
                    documentService.get(token.getDocument(), result -> {
                        if (result.isRight()) {
                            JsonObject document = result.right().getValue();
                            getRedirectionUrl(request, token, document, event -> {
                                if (event.isRight()) {
                                    JsonObject params = new JsonObject()
                                            .put("lool-redirection", event.right().getValue())
                                            .put("document-id", token.getDocument())
                                            .put("access-token", token.getId())
                                            .put("resync", request.params().contains("resync") ? request.getParam("resync") : false);
                                    renderView(request, params, "lool.html", null);
                                    eventStore.createAndStoreEvent(Actions.ACCESS.name(), request);
                                    TraceHelper.add(Actions.ACCESS.name(), token.getUser(), token.getDocument(), TraceHelper.getFileExtension(document.getString("name")));
                                } else {
                                    renderError(request);
                                }
                            });
                        } else {
                            renderError(request);
                        }
                    });
                } else {
                    unauthorized(request);
                }
            });
        });
    }


    /**
     * Get redirection url for Libre Office Online document
     *
     * @param request Server request
     * @param token    User Libre Office Online auth token
     * @param document Document
     * @param handler Function handler returning data
     */
    private void getRedirectionUrl(HttpServerRequest request, Token token, JsonObject document, Handler<Either<String, String>> handler) {
        Lool.wopiHelper.getActionUrl(document.getJsonObject("metadata").getString("content-type"), null, event -> {
            if (event.isRight()) {
                String url = event.right().getValue();
                String redirectURL = url +
                        "WOPISrc=" + Lool.wopiHelper.encodeWopiParam(getScheme(request) + "://" + getHost(request) + "/lool/wopi/files/" + document.getString("_id")) +
//                        "WOPISrc=" + Lool.wopiHelper.encodeWopiParam("https://nginx/lool/wopi/files/" + document.getString("_id")) +
                        "&title=" + Lool.wopiHelper.encodeWopiParam(document.getString("name")) +
                        "&access_token=" + token.getId() +
                        "&lang=fr" +
                        "&closebutton=0" +
                        "&revisionhistory=1";
                handler.handle(new Either.Right<>(redirectURL));
            } else {
                String message = "[LoolController@redirectToLool] Failed to redirect to Libre Office Online for document " + document.getString("_id");
                log.error(message);
                handler.handle(new Either.Left<>(message));
            }
        });
    }

    @Get("/capabilities")
    @ApiDoc("Get all Libre Office Online file capabilities")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCapabilities(HttpServerRequest request) {
        Lool.wopiHelper.getCapabilities(arrayResponseHandler(request));
    }

    @Get("/discover")
    public void discover(HttpServerRequest request) {
        Lool.wopiHelper.discover(aBoolean -> request.response().setStatusCode(201).end("201 Created"));
    }

    @Get("/documents/:id/tokens")
    @ApiDoc("Generate provisional token for given document")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void generateDocumentToken(HttpServerRequest request) {
        if (!request.params().contains("access_token") && !request.params().contains("image")) {
            badRequest(request);
            return;
        }
        String accessToken = request.getParam("access_token");
        String image = request.getParam("image");
        String document = request.getParam("id");
        Lool.wopiHelper.validateToken(accessToken, document, Bindings.CONTRIB.toString(), validation -> {
            if (!validation.getBoolean("valid")) {
                unauthorized(request);
                return;
            }
            UserUtils.getUserInfos(eb, request, user -> Lool.wopiHelper.userCanRead(CookieHelper.getInstance().getSigned("oneSessionId", request), image, canRead -> {
                if (canRead) {
                    tokenService.create(request.getParam("id"), user.getUserId(), either -> {
                        if (either.isRight()) {
                            renderJson(request, new JsonObject().put("_id", either.right().getValue().getString("_id")), 201);
                        } else {
                            renderError(request);
                        }
                    });
                } else {
                    unauthorized(request);
                }
            }));
        });
    }

    @Get("/documents/:id/image/:imageId")
    @ApiDoc("Get all Libre Office Online file capabilities")
    public void getDocument(HttpServerRequest request) {
        if (!request.params().contains("access_token") && !request.params().contains("token")) {
            badRequest(request);
            return;
        }
        String documentId = request.getParam("id");
        String imageId = request.getParam("imageId");
        String accessToken = request.getParam("access_token");
        String token = request.getParam("token");
        Lool.wopiHelper.validateToken(accessToken, documentId, Bindings.CONTRIB.toString(), validation -> {
            if (!validation.getBoolean("valid")) {
                unauthorized(request);
                return;
            }
            Token loolToken = new Token(validation.getJsonObject("token"));

            tokenService.get(token, either -> {
                if (either.isRight()) {
                    if (loolToken.getUser().equals(either.right().getValue().getString("user"))) {
                        tokenService.delete(token, deleteEither -> {
                            if (deleteEither.isRight()) {
                                documentService.get(imageId, event -> {
                                    if (event.isRight()) {
                                        JsonObject image = event.right().getValue();
                                        fileService.get(image.getString("file"), buffer -> request.response()
                                                .setStatusCode(200)
                                                .putHeader("Content-Type", "application/octet-stream")
                                                .putHeader("Content-Transfer-Encoding", "Binary")
                                                .putHeader("Content-disposition", "attachment; filename=" + image.getString("name"))
                                                .end(buffer));
                                    } else {
                                        renderError(request);
                                    }
                                });
                            } else {
                                renderError(request);
                            }
                        });
                    } else {
                        unauthorized(request);
                    }
                } else {
                    renderError(request);
                }
            });
        });
    }

    @Get("/document")
    @ApiDoc("Create new document based on lool templates")
    @SecuredAction("create.document")
    public void createDocumentFromTemplate(HttpServerRequest request) {
        if (!request.params().contains("type") && !request.params().contains("name")) {
            badRequest(request);
            return;
        }
        UserUtils.getUserInfos(eb, request, user -> {
            request.pause();
            String path = FileResolver.absolutePath("public/lool-templates/");
            String type = request.getParam("type");
            String filePath = path + "template." + type;
            String filename = request.getParam("name") + "." + type;
            String folder = request.getParam("folder");
            String contentType;
            try {
                contentType = Files.probeContentType(Paths.get(filePath));
            } catch (IOException e) {
                log.error("[LoolController@createDocumentFromTemplate] Failed to read template." + type + " content type", e);
                renderError(request);
                return;
            }
            vertx.fileSystem().readFile(filePath, readEvent -> {
                if (readEvent.succeeded()) {
                    Buffer fileBuffer = readEvent.result();
                    fileService.add(fileBuffer, contentType, filename, either -> {
                        if (either.isLeft()) {
                            log.error("[LoolController@createDocumentFromTemplate] Failed to insert file in file system from buffer");
                            renderError(request);
                            return;
                        }
                        JsonObject file = either.right().getValue();
                        this.workspaceHelper.addDocument(file, user, filename, "media-library", false, new JsonArray(), handlerToAsyncHandler(message -> {
                            if ("ok".equals(message.body().getString("status"))) {
                                String documentId = message.body().getString("_id");
                                if (folder != null) {
                                    workspaceHelper.moveDocument(documentId, folder, user, res -> {
                                        redirect(request, "/lool/documents/" + documentId + "/open?resync=true");
                                    });
                                } else {
                                    redirect(request, "/lool/documents/" + documentId + "/open?resync=true");
                                }
                            } else {
                                renderError(request);
                            }
                        }));
                    });
                } else {
                    log.error("[LoolController@createDocumentFromTemplate] Failed to read file : " + filePath, readEvent.cause());
                }
            });
        });

    }

    public void cleanDocumentsToken(Handler<Boolean> handler) {
        tokenService.clean(handler);
    }
}
