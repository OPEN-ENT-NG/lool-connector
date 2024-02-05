package fr.openent.lool.provider;

import fr.openent.lool.bean.ActionURL;
import fr.openent.lool.core.constants.Field;
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
    public String redirectURL(HttpServerRequest request, ActionURL actionURL, JsonObject document, Wopi wopiService) {
        return actionURL.url().getProtocol() + "://" + actionURL.url().getHost() + actionURL.url().getPath() +
                "?WOPISrc=" + wopiService.helper().encodeWopiParam(getScheme(request) + "://" + getHost(request) + "/lool/wopi/files/" + document.getString(Field._ID)) +
                "&title=" + wopiService.helper().encodeWopiParam(document.getString(Field.NAME)) +
                "&lang=fr" +
                "&closebutton=0" +
                "&revisionhistory=1";
    }
}
