package fr.openent.lool.provider;

import fr.openent.lool.bean.ActionURL;
import fr.openent.lool.core.constants.Field;
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
        return actionURL.url().getProtocol() + "://" + actionURL.url().getAuthority() + actionURL.url().getPath() +
                "?lang=fr" +
                "&wopisrc=" + Wopi.getInstance().helper().encodeWopiParam(Renders.getScheme(request) + "://" + Renders.getHost(request) + "/lool/wopi/files/" + document.getString(Field._ID));
//                "&wopisrc=" + Wopi.getInstance().helper().encodeWopiParam(getScheme(request) + "://" + "vertx:8090" + "/lool/wopi/files/" + document.getString(Field._ID));
    }

}
