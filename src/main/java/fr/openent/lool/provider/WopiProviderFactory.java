package fr.openent.lool.provider;

import java.net.MalformedURLException;
import java.net.URL;

public class WopiProviderFactory {

    private WopiProviderFactory() {
        throw new IllegalStateException("Utility class");
    }

    public static WopiProvider provider(WopiProviders type, URL url) throws MalformedURLException {
        switch (type) {
            case OnlyOffice:
                return new OnlyOffice(url);
            case LibreOfficeOnline:
                return new LibreOfficeOnline(url);
            default:
                return null;
        }
    }
}
