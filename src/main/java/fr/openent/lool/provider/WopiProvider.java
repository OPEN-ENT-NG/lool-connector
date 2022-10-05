package fr.openent.lool.provider;

import fr.openent.lool.bean.ActionURL;
import fr.openent.lool.core.constants.Field;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
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
import java.net.URL;

public abstract class WopiProvider {
    private final Logger log = LoggerFactory.getLogger(WopiProvider.class);
    private final URL url;
    private final WopiProviders type;

    protected WopiProvider(WopiProviders type, URL url) {
        this.type = type;
        this.url = url;
    }

    public abstract String redirectURL(HttpServerRequest request, ActionURL actionURL, JsonObject document);

    public URL url() {
        return this.url;
    }

    public WopiProviders type() {
        return this.type;
    }

    public boolean revokeToken() {
        return true;
    }

    public JsonArray parseDiscovery(Buffer buffer) {
        JsonArray result = new JsonArray();
        try (StringReader reader = new StringReader(new String(buffer.getBytes()))) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource source = new InputSource(reader);
            Document xml = builder.parse(source);
            xml.getDocumentElement().normalize();

            NodeList actions = xml.getElementsByTagName("app");

            for (int i = 0; i < actions.getLength(); i++) {
                Element app = (Element) actions.item(i);
                NodeList subActions = app.getElementsByTagName("action");
                for (int j = 0; j < subActions.getLength(); j++) {
                    Element action = (Element) subActions.item(j);
                    String contentType = app.getAttribute(Field.NAME);
                    String extension = action.getAttribute("ext");
                    String actionName = action.getAttribute(Field.NAME);
                    String urlSrc = action.getAttribute("urlsrc");
                    result.add(
                            new JsonObject()
                                    .put("content-type", contentType)
                                    .put("extension", extension)
                                    .put("action", actionName)
                                    .put("url", urlSrc)
                    );
                }
            }

        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.error("[WopiProvider@parseDiscover] An error occurred while parsing discovery file", e);
        }

        return result;
    }
}
