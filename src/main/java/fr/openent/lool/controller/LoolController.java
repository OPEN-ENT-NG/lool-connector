package fr.openent.lool.controller;

import fr.openent.lool.Lool;
import fr.openent.lool.bean.Token;
import fr.openent.lool.service.DocumentService;
import fr.openent.lool.service.Impl.DefaultDocumentService;
import fr.openent.lool.service.Impl.DefaultTokenService;
import fr.openent.lool.service.TokenService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.controller.ControllerHelper;
import org.entcore.common.user.UserUtils;

public class LoolController extends ControllerHelper {

    private DocumentService documentService;
    private TokenService tokenService = new DefaultTokenService();

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


    private void redirectToLool(HttpServerRequest request, Token token, JsonObject document) {
        // REDIRECT URL : https://rdoffice.arawa.fr/loleaflet/ffc419a/loleaflet.html?WOPISrc='.urlencode(redir()).'%2Fwopi%2F
        // discovery_url + "WOPISrc=" + ENT_URL + "/lool/" + document_id + "&title=" + docbument_title + "&lang=fr&closebutton=0&revisionhistory=1"
        String redirectURL = Lool.wopiHelper.getActionUrl() +
//                "WOPISrc=" + Lool.wopiHelper.encodeWopiParam(getScheme(request) + "://nginx/lool/wopi/files/" + document.getString("_id")) +
                "WOPISrc=" + Lool.wopiHelper.encodeWopiParam("https://nginx/lool/wopi/files/" + document.getString("_id")) +
                "&title=" + Lool.wopiHelper.encodeWopiParam(document.getString("name")) +
                "&access_token=" + token.getId() +
                "&lang=fr" +
                "&closebutton=0" +
                "&revisionhistory=1";
        request.response().setStatusCode(302);
        request.response().putHeader("Location", redirectURL);
        request.response().end();
    }

    @Get("/discover")
    public void discover(HttpServerRequest request) {
        Lool.wopiHelper.discover(aBoolean -> request.response().end(Lool.wopiHelper.getActionUrl()));
    }
}
