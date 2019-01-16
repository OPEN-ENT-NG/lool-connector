package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.bean.Token;
import fr.openent.lool.service.DocumentService;
import fr.openent.lool.service.Impl.DefaultDocumentService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
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

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class LoolController extends ControllerHelper {

    private final DocumentService documentService;

    public LoolController(EventBus eb, Storage storage) {
        super();
        documentService = new DefaultDocumentService(eb, storage);
    }

    @Get("/documents/:id/open")
    @ApiDoc("Open document in Libre Office Online")
    @SecuredAction("lool.open.file")
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
                            getRedirectionUrl(token, result.right().getValue(), event -> {
                                if (event.isRight()) {
                                    JsonObject params = new JsonObject()
                                            .put("lool-redirection", event.right().getValue());
                                    renderView(request, params, "lool.html", null);
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
     * @param token    User Libre Office Online auth token
     * @param document Document
     * @param handler Function handler returning data
     */
    private void getRedirectionUrl(Token token, JsonObject document, Handler<Either<String, String>> handler) {
        Lool.wopiHelper.getActionUrl(document.getJsonObject("metadata").getString("content-type"), null, event -> {
            if (event.isRight()) {
                String url = event.right().getValue();
                String redirectURL = url +
//                        "WOPISrc=" + Lool.wopiHelper.encodeWopiParam(getScheme(request) + "://" + getHost(request) + "/lool/wopi/files/" + document.getString("_id")) +
                        "WOPISrc=" + Lool.wopiHelper.encodeWopiParam("https://nginx/lool/wopi/files/" + document.getString("_id")) +
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
}
