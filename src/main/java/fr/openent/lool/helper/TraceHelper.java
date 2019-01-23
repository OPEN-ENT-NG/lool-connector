package fr.openent.lool.helper;

import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.json.JsonObject;

public class TraceHelper {
    static String TRACE_COLLECTION = "lool_events";

    /**
     * Insert trace based on given event, given user and given document
     *
     * @param event     event name
     * @param user      user identifier
     * @param document  document identifier
     * @param extension file extension
     */
    public static void add(String event, String user, String document, String extension) {
        JsonObject trace = new JsonObject()
                .put("event", event)
                .put("user", user)
                .put("document", document)
                .put("extension", extension);

        add(trace);
    }

    /**
     * Insert trace based on given trace
     *
     * @param trace trace to insert
     */
    public static void add(JsonObject trace) {
        MongoDb.getInstance().insert(TRACE_COLLECTION, trace);
    }

    public static String getFileExtension(String filename) {
        if (!filename.contains(".")) {
            return "";
        }

        String[] split = filename.split("\\.");
        return split[split.length - 1];
    }
}
