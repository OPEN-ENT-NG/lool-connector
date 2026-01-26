package fr.openent.lool.provider;

import fr.openent.lool.bean.WopiConfig;
import fr.openent.lool.helper.WopiHelper;

public class Wopi {
    private WopiProvider provider;
    private WopiConfig config;
    private WopiHelper helper;
    private String id;


    private Wopi() {
    }

    public Wopi(WopiProvider provider, WopiConfig config,  WopiHelper helper, String id) {
        this.provider = provider;
        this.config = config;
        this.helper = helper;
        this.id = id;
    }

    public WopiProvider provider() {
        return this.provider;
    }

    public WopiConfig config() {
        return this.config;
    }

    public WopiHelper helper() {
        return this.helper;
    }

    public String id() {
        return this.id;
    }

}
