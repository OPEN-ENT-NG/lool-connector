package fr.openent.lool.service.Impl;

import fr.openent.lool.core.constants.Field;
import fr.openent.lool.service.MonitoringService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Utils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DefaultMonitoringService implements MonitoringService {

    private Logger log = LoggerFactory.getLogger(DefaultMonitoringService.class);

    @Override
    public void getDocuments(Handler<Either<String, JsonArray>> handler) {
        JsonArray pipeline = new JsonArray();
        JsonObject aggregation = new JsonObject()
                .put("aggregate", "wopi_token")
                .put("allowDiskUse", true)
                .put("cursor", new JsonObject())
                .put("pipeline", pipeline);
        JsonArray arrayElemAt = new JsonArray().add("$filename").add(0);
        JsonObject group = new JsonObject()
                .put(Field._ID, "$document")
                .put("users", new JsonObject().put("$sum", 1))
                .put("filename", new JsonObject().put("$push", "$filename"));
        JsonObject project = new JsonObject()
                .put(Field._ID, "$_id")
                .put("filename", new JsonObject().put("$arrayElemAt", arrayElemAt))
                .put("users", "$users");
        JsonObject matcher = new JsonObject()
                .put("valid", new JsonObject().put("$exists", false));
        pipeline.add(new JsonObject().put("$match", matcher))
                .add(new JsonObject().put("$group", group))
                .add(new JsonObject().put("$project", project));
        MongoDb.getInstance().command(aggregation.toString(), message -> {
            JsonObject body = message.body();
            if (Field.OK.equals(body.getString(Field.STATUS))) {
                handler.handle(new Either.Right<>(body.getJsonObject("result").getJsonObject("cursor").getJsonArray("firstBatch")));
            } else {
                String error = "[DefaultMonitoringService@getDocuments] Failed to fetch documents";
                log.error(error);
                handler.handle(new Either.Left<>(error));
            }
        });
    }

    @Override
    public void countUsers(Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject()
                .put("valid", new JsonObject().put("$exists", false));
        MongoDb.getInstance().count("wopi_token", matcher, message -> handler.handle(Utils.validResult(message)));
    }

    @Override
    public void countEvent(String event, Handler<Either<String, JsonObject>> handler) {
        MongoDb.getInstance().count("lool_events", new JsonObject().put("event", event), message -> handler.handle(Utils.validResult(message)));
    }

    @Override
    public void getExtensions(Handler<Either<String, JsonArray>> handler) {
        //TODO @Sled. Calculer en %. Max 5 retours.
        JsonArray pipeline = new JsonArray();
        JsonObject aggregation = new JsonObject()
                .put("aggregate", "lool_events")
                .put("allowDiskUse", true)
                .put("cursor", new JsonObject())
                .put("pipeline", pipeline);
        JsonObject group = new JsonObject()
                .put(Field._ID, "$extension")
                .put("count", new JsonObject().put("$sum", 1));
        JsonObject sort = new JsonObject()
                .put("$sort", new JsonObject().put("count", -1));
        JsonObject projectionFields = new JsonObject()
                .put(Field._ID, "$_id")
                .put("count", "$count");
        pipeline.add(new JsonObject().put("$match", new JsonObject()))
                .add(new JsonObject().put("$group", group))
                .add(sort)
                .add(new JsonObject().put("$project", projectionFields));
        MongoDb.getInstance().command(aggregation.toString(), message -> {
            JsonObject body = message.body();
            if (Field.OK.equals(body.getString(Field.STATUS))) {
                handler.handle(new Either.Right<>(body.getJsonObject("result").getJsonObject("cursor").getJsonArray("firstBatch")));
            } else {
                String error = "[DefaultMonitoringService@getExtensions] Failed to fetch extensions";
                log.error(error);
                handler.handle(new Either.Left<>(error));
            }
        });
    }
}
