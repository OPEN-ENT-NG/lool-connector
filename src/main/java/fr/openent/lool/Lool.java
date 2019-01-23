package fr.openent.lool;

import fr.openent.lool.controller.LoolController;
import fr.openent.lool.controller.MonitoringController;
import fr.openent.lool.controller.WopiController;
import fr.openent.lool.helper.WopiHelper;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Lool extends BaseServer {

    public static WopiHelper wopiHelper;

	@Override
	public void start() throws Exception {
		super.start();
        wopiHelper = new WopiHelper(vertx, config.getString("lool-server"));
        wopiHelper.setConfig(config.getJsonObject("lool-config", new JsonObject()));

        EventBus eb = vertx.eventBus();
        Storage storage = new StorageFactory(vertx, config).getStorage();
        LoolController loolController = new LoolController(eb, storage);
        addController(loolController);
        addController(new WopiController(eb, storage));
        addController(new MonitoringController());
        vertx.setTimer(30000, aLong -> wopiHelper.discover(status -> log.info("Libre Office Online discover " + (status ? "OK" : "KO"))));
        vertx.setTimer(30000, timer -> wopiHelper.clearTokens(status -> log.info("Libre Office Online clear token " + (status.isRight() ? "OK" : "KO"))));
        vertx.setTimer(30000, timer -> loolController.cleanDocumentsToken(status -> log.info("Libre Office Online document tokens " + (status ? "OK" : "KO"))));
    }
}
