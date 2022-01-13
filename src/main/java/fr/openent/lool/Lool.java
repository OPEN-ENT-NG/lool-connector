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
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

import java.util.Objects;

public class Lool extends BaseServer {

    private static final int WAITING_TIME = 30000;

    @Override
    public void start() throws Exception {
        super.start();

        WopiConfig wopiConfig;
        WopiProvider provider;

        try {
            wopiConfig = WopiConfig.from(config.getJsonObject("wopi", new JsonObject()));
            provider = WopiProviderFactory.provider(wopiConfig.type(), wopiConfig.server());
        } catch (NullPointerException e) {
            throw new InvalidWopiServerException(e);
        }

        if (Objects.isNull(provider)) {
            throw new InvalidWopiProviderException();
        }

        Wopi.getInstance().init(provider, wopiConfig).setHelper(new WopiHelper(vertx));

        EventBus eb = vertx.eventBus();
        Storage storage = new StorageFactory(vertx, config).getStorage();
        LoolController loolController = new LoolController(eb, storage, wopiConfig);
        addController(loolController);
        addController(new WopiController(eb, storage));
        addController(new MonitoringController());
        vertx.setTimer(WAITING_TIME, aLong -> Wopi.getInstance().helper().discover(status -> log.info("Libre Office Online discover " + (Boolean.TRUE.equals(status) ? "OK" : "KO"))));
        vertx.setTimer(WAITING_TIME, timer -> Wopi.getInstance().helper().clearTokens(status -> log.info("Libre Office Online clear token " + (status.isRight() ? "OK" : "KO"))));
        vertx.setTimer(WAITING_TIME, timer -> loolController.cleanDocumentsToken(status -> log.info("Libre Office Online document tokens " + (Boolean.TRUE.equals(status) ? "OK" : "KO"))));
    }
}
