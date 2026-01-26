package fr.openent.lool.controller;

import fr.openent.lool.service.Impl.DefaultMonitoringService;
import fr.openent.lool.service.MonitoringService;
import fr.wseduc.rs.ApiDoc;
import fr.wseduc.rs.Get;
import fr.wseduc.security.ActionType;
import fr.wseduc.security.SecuredAction;
import io.vertx.core.http.HttpServerRequest;
import org.entcore.common.controller.ControllerHelper;

import static org.entcore.common.http.response.DefaultResponseHandler.arrayResponseHandler;
import static org.entcore.common.http.response.DefaultResponseHandler.defaultResponseHandler;

public class MonitoringController extends ControllerHelper {

    private final MonitoringService monitoringService = new DefaultMonitoringService();

    @Get("/dashboard")
    @ApiDoc("Render monitoring view")
    @SecuredAction("monitoring")
    public void monitoring(HttpServerRequest request) {
        renderView(request, null, "monitoring.html", null);
    }

    @Get("/monitoring/documents")
    @ApiDoc("Retrieve opened documents and user number")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getDocuments(HttpServerRequest request) {
        monitoringService.getDocuments(arrayResponseHandler(request));
    }

    @Get("/monitoring/users/count")
    @ApiDoc("Retrieve count users")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void countUsers(HttpServerRequest request) {
        monitoringService.countUsers(defaultResponseHandler(request));
    }

    @Get("/monitoring/events/:event/count")
    @ApiDoc("Retrieve event count")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void countEvent(HttpServerRequest request) {
        monitoringService.countEvent(request.getParam("event"), defaultResponseHandler(request));
    }

    @Get("/monitoring/extensions")
    @ApiDoc("Retrieve extensions count")
    @SecuredAction(value = "", type = ActionType.AUTHENTICATED)
    public void getExtentions(HttpServerRequest request) {
        monitoringService.getExtensions(arrayResponseHandler(request));
    }
}
