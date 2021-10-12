package fr.openent.lool.helper;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpHelper {
    private final Vertx vertx;

    public HttpHelper(Vertx vertx) {
        this.vertx = vertx;
    }

    /**
     * Generate Http client
     *
     * @param uri URI used for Http client
     * @return Http client
     */
    public HttpClient generateHttpClient(URL uri) {
        HttpClientOptions options = new HttpClientOptions()
                .setDefaultHost(uri.getHost())
//                .setDefaultPort("https".equals(uri.getScheme()) ? 9980 : 80)
                .setDefaultPort("https".equals(uri.getProtocol()) ? 443 : 80)
                .setVerifyHost(false)
                .setTrustAll(true)
                .setSsl("https".equals(uri.getProtocol()))
                .setKeepAlive(true);

        return vertx.createHttpClient(options);
    }

    /**
     * Encode parameter as HTTP parameter
     *
     * @param param Parameter to encode
     * @return Parameter encoded as HTTP parameter
     */
    String encode(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
