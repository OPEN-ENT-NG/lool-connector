package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.bean.ActionURL;
import fr.openent.lool.bean.Token;
import fr.openent.lool.bean.WopiConfig;
import fr.openent.lool.core.constants.Field;
import fr.openent.lool.helper.TraceHelper;
import fr.openent.lool.helper.WopiHelper;
import fr.openent.lool.provider.Wopi;
import fr.openent.lool.provider.WopisProviders;
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
import fr.wseduc.webutils.http.Renders;
import fr.wseduc.webutils.request.CookieHelper;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.entcore.common.bus.WorkspaceHelper;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.events.EventStore;
import org.entcore.common.events.EventStoreFactory;
import org.entcore.common.http.filter.AdminFilter;
import org.entcore.common.http.filter.ResourceFilter;
import org.entcore.common.http.filter.SuperAdminFilter;
import org.entcore.common.storage.Storage;
import org.entcore.common.user.UserUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

public class LoolController extends ControllerHelper {

    private final DocumentService documentService;
    private final FileService fileService;
    private final TokenService tokenService;
    private final EventStore eventStore;
    private final WorkspaceHelper workspaceHelper;

    public LoolController(EventBus eb, Storage storage) {
        super();
        documentService = new DefaultDocumentService(eb, storage);
        fileService = new DefaultFileService(storage);
        tokenService = new DefaultTokenService();
        eventStore = EventStoreFactory.getFactory().getEventStore(Lool.class.getSimpleName());
        this.workspaceHelper = new WorkspaceHelper(eb, storage);
    }

    @Get("")
    @ApiDoc("Render view")
    @SecuredAction("view")
    public void render(HttpServerRequest request) {
        renderView(request, new JsonObject(), "lool-home.html", null);
    }

    @Get("/config")
    @SecuredAction(value = "", type = ActionType.RESOURCE)
    @ResourceFilter(AdminFilter.class)
    public void getConfig(final HttpServerRequest request) {
        renderJson(request, config);
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
            final Wopi wopiService = WopisProviders.getProvider(Renders.getHost(request));
            wopiService.helper().generateLoolToken(request, tokenEvent -> {
                if (tokenEvent.isRight()) {
                    Token token = tokenEvent.right().getValue();
                    documentService.get(token.getDocument(), result -> {
                        if (result.isRight()) {
                            JsonObject document = result.right().getValue();
                            getRedirectionUrl(request, document, wopiService, event -> {
                                if (event.isRight()) {
                                    Timestamp ts = Timestamp.from(Instant.now());
                                    Duration d = Duration.ofHours(wopiService.config().duration_token());
                                    long duration_token = d.toMillis();
                                    JsonObject params = new JsonObject()
                                            .put("redirection", event.right().getValue())
                                            .put("document-id", token.getDocument())
                                            .put("access-token", token.getId())
                                            .put("server", wopiService.config().server().toString())
                                            .put("resync", request.params().contains("resync") ? request.getParam("resync") : false)
                                            .put("provider-name",wopiService.provider().type())
                                            .put("duration-token",duration_token + ts.getTime());
                                    renderView(request, params, "doc.html", null);
                                    eventStore.createAndStoreEvent(Actions.ACCESS.name(), request);
                                    TraceHelper.add(Actions.ACCESS.name(), token.getUser(), token.getDocument(), TraceHelper.getFileExtension(document.getString(Field.NAME)));
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
     * @param document Document
     * @param handler Function handler returning data
     */
    private void getRedirectionUrl(HttpServerRequest request, JsonObject document, Wopi wopiService, Handler<Either<String, String>> handler) {
        wopiService.helper().getActionUrl(document.getJsonObject(Field.METADATA).getString("content-type"), null, event -> {
            if (event.isRight()) {
                ActionURL actionURL = event.right().getValue();
                handler.handle(new Either.Right<>(wopiService.provider().redirectURL(request, actionURL, document, wopiService)));
            } else {
                String message = "[LoolController@redirectToLool] Failed to redirect to Libre Office Online for document " + document.getString(Field._ID);
                log.error(message, event.left().getValue());
                handler.handle(new Either.Left<>(message));
            }
        });
    }

    @Get("/providers/context")
    @ApiDoc("Get Wopi provider context")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getCapabilities(HttpServerRequest request) {
        final Wopi wopiService = WopisProviders.getProvider(Renders.getHost(request));
        wopiService.helper().getCapabilities()
                .onFailure(failure -> {
                    log.error("Fail to fetch wopi provider capabilities", failure);
                    renderError(request);
                })
                .onSuccess(capabilities -> {
                    JsonObject body = new JsonObject()
                            .put("provider", wopiService.provider().type())
                            .put("capabilities", capabilities)
                            .put("templates", wopiService.config().templates());
                    renderJson(request, body);
                });
    }

    @Get("/discover")
    @ResourceFilter(SuperAdminFilter.class)
    @SecuredAction(value ="", type = ActionType.RESOURCE)
    public void discover(HttpServerRequest request) {
        final Wopi wopiService = WopisProviders.getProvider(Renders.getHost(request));
        wopiService.helper().discover(wopiService, aBoolean -> request.response().setStatusCode(201).end("201 Created"));
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
        String document = request.getParam(Field.ID);
        final Wopi wopiService = WopisProviders.getProvider(Renders.getHost(request));
        wopiService.helper().validateToken(accessToken, document, Bindings.CONTRIB.toString(), validation -> {
            if (!validation.getBoolean("valid")) {
                unauthorized(request);
                return;
            }
            UserUtils.getUserInfos(eb, request, user -> wopiService.helper().userCanRead(CookieHelper.getInstance().getSigned("oneSessionId", request), image, canRead -> {
                if (canRead) {
                    tokenService.create(request.getParam(Field.ID), user.getUserId(), either -> {
                        if (either.isRight()) {
                            renderJson(request, new JsonObject().put(Field._ID, either.right().getValue().getString(Field._ID)), 201);
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
        if (!request.params().contains("access_token") && !request.params().contains(Field.TOKEN)) {
            badRequest(request);
            return;
        }
        String documentId = request.getParam(Field.ID);
        String imageId = request.getParam("imageId");
        String accessToken = request.getParam("access_token");
        String token = request.getParam(Field.TOKEN);
        final Wopi wopiService = WopisProviders.getProvider(Renders.getHost(request));
        wopiService.helper().validateToken(accessToken, documentId, Bindings.CONTRIB.toString(), validation -> {
            if (!validation.getBoolean("valid")) {
                unauthorized(request);
                return;
            }
            Token loolToken = new Token(validation.getJsonObject(Field.TOKEN));

            tokenService.get(token, either -> {
                if (either.isRight()) {
                    if (loolToken.getUser().equals(either.right().getValue().getString("user"))) {
                        tokenService.delete(token, deleteEither -> {
                            if (deleteEither.isRight()) {
                                documentService.get(imageId, event -> {
                                    if (event.isRight()) {
                                        JsonObject image = event.right().getValue();
                                        fileService.get(image.getString(Field.FILE), buffer -> request.response()
                                                .setStatusCode(200)
                                                .putHeader("Content-Type", "application/octet-stream")
                                                .putHeader("Content-Transfer-Encoding", "Binary")
                                                .putHeader("Content-disposition", "attachment; filename=" + image.getString(Field.NAME))
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
        if (!request.params().contains("type") && !request.params().contains(Field.NAME)) {
            badRequest(request);
            return;
        }
        UserUtils.getUserInfos(eb, request, user -> {
            request.pause();
            String path = FileResolver.absolutePath("public/lool-templates/");
            String type = request.getParam("type");
            String filePath = path + "template." + type;
            String filename = request.getParam(Field.NAME) + "." + type;
            String folder = request.getParam("folder");
            // Get protected parameter (default: false for backward compatibility)
            boolean isProtected = request.params().contains(Field.PROTECTED) &&
                    "true".equals(request.getParam(Field.PROTECTED));
            String contentType = getContentType(type, filePath);

            if (contentType == null) {
                log.error("[LoolController@createDocumentFromTemplate] Failed to read contentType. " + type);
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
                        this.workspaceHelper.addDocument(file, user, filename, "media-library", isProtected, new JsonArray(), handlerToAsyncHandler(message -> {
                            if (Field.OK.equals(message.body().getString(Field.STATUS))) {
                                String documentId = message.body().getString(Field._ID);
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

    private String getContentType(String type, String filePath) {
        String contentType;
        try {
            contentType = Files.probeContentType(Paths.get(filePath));
        } catch (IOException e) {
            log.error("[LoolController@getContentType] Failed to read template." + type + " content type", e);
            return null;
        }

        if (contentType == null) {
            switch (type) {
                case Field.PPTX:
                case Field.ODP:
                    contentType = "application/vnd.oasis.opendocument.presentation";
                    break;
                case Field.DOCX:
                case Field.ODT:
                    contentType = "application/vnd.oasis.opendocument.text";
                    break;
                case Field.XLSX:
                case Field.ODS:
                    contentType = "application/vnd.oasis.opendocument.spreadsheet";
                    break;
                case Field.ODG:
                    contentType = "application/vnd.oasis.opendocument.graphics";
                    break;
                default:
                    return null;
            }
        }
        return contentType;
    }

    public void cleanDocumentsToken(Handler<Boolean> handler) {
        tokenService.clean(handler);
    }
}
