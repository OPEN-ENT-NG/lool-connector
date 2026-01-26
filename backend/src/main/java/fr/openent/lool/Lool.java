package fr.openent.lool;

import fr.openent.lool.bean.WopiConfig;
import fr.openent.lool.controller.LoolController;
import fr.openent.lool.controller.MonitoringController;
import fr.openent.lool.controller.WopiController;
import fr.openent.lool.exception.InvalidWopiProviderException;
import fr.openent.lool.exception.InvalidWopiServerException;
import fr.openent.lool.helper.WopiHelper;
import fr.openent.lool.provider.Wopi;
import fr.openent.lool.provider.WopiProvider;
import fr.openent.lool.provider.WopiProviderFactory;
import fr.openent.lool.provider.WopisProviders;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lool extends BaseServer {

    private static final int WAITING_TIME = 30000;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        super.start(startPromise);

        final JsonObject wopi = config.getJsonObject("wopi", new JsonObject());
        final List<Wopi> wopiDiscover = new ArrayList<>();

        if (wopi.containsKey("providers")) {
            final JsonObject providers = wopi.getJsonObject("providers", new JsonObject());
            for (String providerId : providers.getMap().keySet()) {
                WopiConfig wopiConfig;
                WopiProvider provider;

                try {
                    wopiConfig = WopiConfig.from(providers.getJsonObject(providerId));
                    provider = WopiProviderFactory.provider(wopiConfig.type(), wopiConfig.server());
                } catch (NullPointerException e) {
                    throw new InvalidWopiServerException(e);
                }

                if (Objects.isNull(provider)) {
                    throw new InvalidWopiProviderException();
                }

                final Wopi wopiService = new Wopi(provider, wopiConfig, new WopiHelper(vertx,wopiConfig.server(), wopiConfig.type(),providerId), providerId);

                final JsonObject mappings = wopi.getJsonObject("mappings", new JsonObject());
                for (String host : mappings.getMap().keySet()) {
                    if (providerId.equals(mappings.getString(host))) {
                        WopisProviders.addProvider(host, wopiService);
                    }
                }
                wopiDiscover.add(wopiService);
            }
        } else {
            WopiConfig wopiConfig;
            WopiProvider provider;
            final String providerId = "wopi1";

            try {
                wopiConfig = WopiConfig.from(wopi);
                provider = WopiProviderFactory.provider(wopiConfig.type(), wopiConfig.server());
            } catch (NullPointerException e) {
                throw new InvalidWopiServerException(e);
            }

            if (Objects.isNull(provider)) {
                throw new InvalidWopiProviderException();
            }

            final Wopi wopiService = new Wopi(provider, wopiConfig, new WopiHelper(vertx, wopiConfig.server(), wopiConfig.type(), providerId), providerId);
            final String host = config.getString("host").split("//")[1];
            WopisProviders.addProvider(host, wopiService);
            wopiDiscover.add(wopiService);
        }

        EventBus eb = vertx.eventBus();
        Storage storage = new StorageFactory(vertx, config).getStorage();
        LoolController loolController = new LoolController(eb, storage);
        addController(loolController);
        addController(new WopiController(eb, storage));
        addController(new MonitoringController());

        startPromise.tryComplete();
        startPromise.tryFail("[LOOL@Lool::start] Fail to start Lool");

        for (Wopi wp : wopiDiscover) {
            vertx.setTimer(WAITING_TIME, aLong -> wp.helper().discover(wp, status -> log.info(wp.config().type().name() + " discover " + wp.id() + (Boolean.TRUE.equals(status) ? " OK" : " KO"))));
        }
        vertx.setTimer(WAITING_TIME, timer -> WopisProviders.getFistProvider().helper().clearTokens(status -> log.info("Libre Office Online clear token " + (status.isRight() ? "OK" : "KO"))));
        vertx.setTimer(WAITING_TIME, timer -> loolController.cleanDocumentsToken(status -> log.info("Libre Office Online document tokens " + (Boolean.TRUE.equals(status) ? "OK" : "KO"))));
    }
}
