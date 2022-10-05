package fr.openent.lool.bean;

import fr.openent.lool.core.constants.Field;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.CookieHelper;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserUtils;

import java.util.UUID;

import static fr.wseduc.webutils.Utils.handlerToAsyncHandler;

public class Token {
    private final String _id;
    private String user;
    private final String document;
    private final String sessionId;
    private String displayName;
    private final JsonObject date;
    private String filename;
    private boolean valid;

    public Token(EventBus eb, HttpServerRequest request, Handler<Either<String, Token>> handler) {
        this.document = request.getParam(Field.ID);
        this.date = MongoDb.now();
        this.sessionId = CookieHelper.getInstance().getSigned("oneSessionId", request);
        this._id = UUID.randomUUID().toString();
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                this.user = user.getUserId();
                this.displayName = user.getUsername();
                JsonObject action = new JsonObject()
                        .put("action", "getDocument")
                        .put(Field.ID, this.document);
                eb.send("org.entcore.workspace", action, handlerToAsyncHandler(message -> {
                    JsonObject body = message.body();
                    if (!Field.OK.equals(body.getString(Field.STATUS))) {
                        handler.handle(new Either.Left<>("[Token@contructor] An error occurred when calling document"));
                    } else {
                        this.filename = message.body().getJsonObject("result").getString(Field.NAME);
                        handler.handle(new Either.Right<>(this));
                    }
                }));
            } else {
                handler.handle(new Either.Left<>("[Token@contructor] User not found"));
            }
        });
    }

    public Token(JsonObject object) {
        this._id = object.getString(Field._ID);
        this.user = object.getString("user");
        this.document = object.getString("document");
        this.sessionId = object.getString("sessionId");
        this.displayName = object.getString("displayName");
        this.date = object.getJsonObject(Field.DATE);
        this.valid = object.getBoolean("valid", true);
    }

    public String getUser() {
        return user;
    }

    public JsonObject toJSON() {
        JsonObject token = new JsonObject()
                .put("user", this.user)
                .put("document", this.document)
                .put("sessionId", this.sessionId)
                .put("displayName", this.displayName)
                .put(Field.DATE, this.date)
                .put("filename", this.filename);
        if (this._id != null) {
            token.put(Field._ID, this._id);
        }
        return token;
    }

    public String getDocument() {
        return document;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getId() {
        return this._id;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getFilename() {
        return filename;
    }

    public boolean isValid() {
        return valid;
    }
}
