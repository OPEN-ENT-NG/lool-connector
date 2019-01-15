package fr.openent.lool.bean;

import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.request.CookieHelper;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import org.entcore.common.user.UserUtils;

import java.util.UUID;

public class Token {
    private String _id;
    private String user;
    private final String document;
    private final String sessionId;
    private String displayName;
    private final Long date;

    public Token(EventBus eb, HttpServerRequest request, Handler<Either<String, Token>> handler) {
        this.document = request.getParam("id");
        this.date = System.currentTimeMillis();
        this.sessionId = CookieHelper.getInstance().getSigned("oneSessionId", request);
        this._id = UUID.randomUUID().toString();
        UserUtils.getUserInfos(eb, request, user -> {
            if (user != null) {
                this.user = user.getUserId();
                this.displayName = user.getUsername();
                handler.handle(new Either.Right<>(this));
            } else {
                handler.handle(new Either.Left<>("[Token@contructor] User not found"));
            }
        });
    }

    public Token(JsonObject object) {
        this._id = object.getString("_id");
        this.user = object.getString("user");
        this.document = object.getString("document");
        this.sessionId = object.getString("sessionId");
        this.displayName = object.getString("displayName");
        this.date = object.getLong("date");
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
                .put("date", this.date);
        if (this._id != null) {
            token.put("_id", this._id);
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
}
