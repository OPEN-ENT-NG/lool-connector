package fr.openent.lool.provider;

import fr.openent.lool.bean.WopiConfig;
import fr.openent.lool.helper.WopiHelper;

public class Wopi {
    private WopiProvider provider;
    private WopiConfig config;
    private WopiHelper helper;

    private Wopi() {
    }

    public static Wopi getInstance() {
        return WopiHolder.instance;
    }

    public Wopi init(WopiProvider provider, WopiConfig config) {
        this.provider = provider;
        this.config = config;

        return this;
    }

    public void setHelper(WopiHelper helper) {
        this.helper = helper;
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

    private static class WopiHolder {
        private static final Wopi instance = new Wopi();

        private WopiHolder() {
            throw new IllegalStateException("Singleton class");
        }
    }
}
