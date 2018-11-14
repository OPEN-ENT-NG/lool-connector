package fr.openent.lool.helper;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class WopiHelper {

    private Logger log = LoggerFactory.getLogger(WopiHelper.class);
    private String server;
    private HttpHelper httpHelper;
    private HttpClient httpClient;
    private String actionUrl;

    public WopiHelper(Vertx vertx, String server) {
        try {
            this.httpHelper = new HttpHelper(vertx);
            this.server = server;
            this.httpClient = httpHelper.generateHttpClient(new URI(server));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String generateLoolToken() {
        return UUID.randomUUID().toString();
    }


    public String getActionUrl() {
        //FIXME Modifier la récupération de l'action url une fois le parsing complété. La récupération se fait en fonction du type de fichier et de l'action
        return actionUrl;
    }

    public void discover(Handler<Boolean> handler) {
        String discoverUri = "/hosting/discovery";
        HttpClientRequest req = httpClient.get(discoverUri, response -> {
            if (response.statusCode() != 200) {
                log.error("An error occurred when discovering wopi api.");
            } else {
                Buffer responseBuffer = new BufferImpl();
                response.handler(responseBuffer::appendBuffer);
                response.endHandler(aVoid -> parseDiscover(responseBuffer, handler));
                response.exceptionHandler(throwable -> handler.handle(false));
            }
        });

        req.end();
    }

    public String getServer() {
        return server;
    }

    private void parseDiscover(Buffer buffer, Handler<Boolean> handler) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(new String(buffer.getBytes())));
            Document xml = builder.parse(is);
            xml.getDocumentElement().normalize();

            //TODO Faire un parsing complet du fichier. Dans un premier temps, nous prenons une URL au hasard
            NodeList actions = xml.getElementsByTagName("action");
            Element action = (Element) actions.item(0);
            actionUrl = action.getAttribute("urlsrc");
            if (handler != null) {
                handler.handle(true);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
            handler.handle(false);
        }
    }

    public String encodeWopiParam(String param) {
        return httpHelper.encode(param);
    }
}
