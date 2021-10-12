package fr.openent.lool.provider;

import fr.openent.lool.bean.ActionURL;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.net.URL;

import static fr.wseduc.webutils.http.Renders.getHost;
import static fr.wseduc.webutils.http.Renders.getScheme;

public class LibreOfficeOnline extends WopiProvider {

    public LibreOfficeOnline(URL url) {
        super(WopiProviders.LibreOfficeOnline, url);
    }

    @Override
    public String redirectURL(HttpServerRequest request, ActionURL actionURL, JsonObject document) {
        return actionURL.url().getProtocol() + "://" + actionURL.url().getHost() + actionURL.url().getPath() +
                "?WOPISrc=" + Wopi.getInstance().helper().encodeWopiParam(getScheme(request) + "://" + getHost(request) + "/lool/wopi/files/" + document.getString("_id")) +
                "&title=" + Wopi.getInstance().helper().encodeWopiParam(document.getString("name")) +
                "&lang=fr" +
                "&closebutton=0" +
                "&revisionhistory=1";
    }
}
