package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.bean.Token;
import fr.openent.lool.service.DocumentService;
import fr.openent.lool.service.Impl.DefaultDocumentService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserUtils;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;

public class LoolController extends ControllerHelper {

    private final DocumentService documentService;

    public LoolController(EventBus eb) {
        super();
        documentService = new DefaultDocumentService(eb);
    }

    @Get("/documents/:id/open")
    @ApiDoc("Open document in Libre Office Online")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
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
                            redirectToLool(request, token, result.right().getValue());
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
     * Redirect request to Libre Office Online.
     *
     * @param request  Client request used to redirect
     * @param token    User Libre Office Online auth token
     * @param document Document
     */
    private void redirectToLool(HttpServerRequest request, Token token, JsonObject document) {
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
                request.response().setStatusCode(302);
                request.response().putHeader("Location", redirectURL);
                request.response().end();
            } else {
                log.error("[LoolController@redirectToLool] Failed to redirect to Libre Office Online for document " + document.getString("_id"));
                badRequest(request);
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
