package fr.openent.lool;

import fr.openent.lool.controller.LoolController;
import fr.openent.lool.controller.WopiController;
import fr.openent.lool.helper.WopiHelper;
import io.vertx.core.eventbus.EventBus;
import org.entcore.common.http.BaseServer;
import org.entcore.common.storage.Storage;
import org.entcore.common.storage.StorageFactory;

public class Lool extends BaseServer {

    public static WopiHelper wopiHelper;

	@Override
	public void start() throws Exception {
		super.start();
        wopiHelper = new WopiHelper(vertx, config.getString("lool-server"));

        EventBus eb = vertx.eventBus();
        Storage storage = new StorageFactory(vertx, config).getStorage();

        addController(new LoolController(eb));
        addController(new WopiController(eb, storage));
        vertx.setTimer(30000, aLong -> wopiHelper.discover(status -> log.info("Libre Office Online discover " + (status ? "OK" : "KO"))));
        vertx.setTimer(30000, timer -> wopiHelper.clearTokens(status -> log.info("Libre Office Online clear token " + (status.isRight() ? "OK" : "KO"))));
    }
}
