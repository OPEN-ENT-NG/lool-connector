package fr.openent.lool.provider;

import fr.openent.lool.bean.ActionURL;
import fr.wseduc.webutils.I18n;
import fr.wseduc.webutils.http.Renders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.net.URL;

public class OnlyOffice extends WopiProvider {

    public OnlyOffice(URL url) {
        super(WopiProviders.OnlyOffice, url);
    }

    @Override
    public boolean revokeToken() {
        return false;
    }

    @Override
    public String redirectURL(HttpServerRequest request, ActionURL actionURL, JsonObject document) {
        return actionURL.url().getProtocol() + "://" + actionURL.url().getHost() + actionURL.url().getPath() +
                "?lang=" + I18n.acceptLanguage(request) +
                "&wopisrc=" + Wopi.getInstance().helper().encodeWopiParam(Renders.getScheme(request) + "://" + Renders.getHost(request) + "/lool/wopi/files/" + document.getString("_id"));
//                "&wopisrc=" + Wopi.getInstance().helper().encodeWopiParam(getScheme(request) + "://" + "vertx:8090" + "/lool/wopi/files/" + document.getString("_id"));
    }

}
