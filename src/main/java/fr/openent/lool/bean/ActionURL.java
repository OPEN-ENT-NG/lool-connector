package fr.openent.lool.bean;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionURL {
    private URL url;
    private Map<String, String> parameters = new HashMap<>();

    public URL url() {
        return this.url;
    }

    public Map<String, String> parameters() {
        return this.parameters;
    }

    public static ActionURL parse(String urlSrc) throws MalformedURLException {
        ActionURL res = new ActionURL();
        res.url = new URL(urlSrc);

        Pattern pattern = Pattern.compile("<(.*?)>");
        Matcher matcher = pattern.matcher(res.url.getQuery());
        while (matcher.find()) {
            String match = matcher.group().replaceAll("[<>&]", "");
            String[] split = match.split("=");
            res.parameters.put(split[1], split[0]);
        }

        return res;
    }
}
