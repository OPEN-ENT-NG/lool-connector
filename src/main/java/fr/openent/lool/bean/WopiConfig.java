package fr.openent.lool.bean;

import fr.openent.lool.provider.WopiProviders;
import io.vertx.core.json.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class WopiConfig {
    private final WopiProviders type;
    private final URL server;
    private final Map<String, Object> serverCapabilities;
    private final List<String> templates;

    public WopiConfig(JsonObject wopiConfig) throws MalformedURLException {
        JsonObject provider = wopiConfig.getJsonObject("provider", new JsonObject());
        this.type = WopiProviders.valueOf(provider.getString("type", null));
        this.server = new URL(provider.getString("url", null));
        this.serverCapabilities = wopiConfig.getJsonObject("server_capabilities", new JsonObject()).getMap();
        this.templates = wopiConfig.containsKey("templates") ? wopiConfig.getJsonArray("templates").getList() : Arrays.asList("odt", "odp", "ods", "odg");
    }

    public WopiProviders type() {
        return this.type;
    }

    public URL server() {
        return this.server;
    }

    public List<String> templates() {
        return this.templates;
    }

    public Map<String, Object> serverCapabilities() {
        return this.serverCapabilities;
    }

    public static WopiConfig from(JsonObject wopiConfig) throws MalformedURLException {
        return new WopiConfig(wopiConfig);
    }
}
