package fr.openent.lool.helper;

import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import fr.openent.lool.bean.ActionURL;
import fr.openent.lool.bean.Token;
import fr.openent.lool.core.constants.Field;
import fr.openent.lool.provider.Wopi;
import fr.openent.lool.provider.WopiProviders;
import fr.openent.lool.utils.Bindings;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Utils;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.BufferImpl;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.entcore.common.mongodb.MongoDbResult;
import org.entcore.common.user.UserUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WopiHelper {

    private static final String DISCOVER_COLLECTION = "lool_discover";
    public static final String TOKEN_COLLECTION = "wopi_token";

    private final Logger log = LoggerFactory.getLogger(WopiHelper.class);
    private final HttpHelper httpHelper;
    private final HttpClient httpClient;
    private final EventBus eb;
    private final String providerId;

    public WopiHelper(Vertx vertx, URL url, WopiProviders type, String providerId) {
        this.httpHelper = new HttpHelper(vertx);
        this.eb = vertx.eventBus();
        this.httpClient = httpHelper.generateHttpClient(url, type);
        this.providerId = providerId;
    }

    /**
     * Returns a Libre Office authentication token.
     *
     * @param request Client request
     * @param handler Function handler returning data
     */
    public void generateLoolToken(HttpServerRequest request, Handler<Either<String, Token>> handler) {
        new Token(eb, request, event -> {
            if (event.isRight()) {
                Token token = event.right().getValue();
                MongoDb.getInstance().save(WopiHelper.TOKEN_COLLECTION, token.toJSON(), saveEvent -> {
                    JsonObject body = saveEvent.body();
                    if (Field.OK.equals(body.getString(Field.STATUS))) {
                        handler.handle(new Either.Right<>(token));
                    } else {
                        handler.handle(new Either.Left<>(body.getString("message")));
                    }
                });
            } else {
                log.error("[WopiHelper@generateLoolToken] Failed to create token", event.left().getValue());
            }

        });
    }


    /**
     * Get action url from the discovery database
     *
     * @param contentType File content-type
     * @param action      Optional. User action
     * @param handler     Function handler returning data
     */
    public void getActionUrl(String contentType, String action, Handler<Either<String, ActionURL>> handler) {
        if (contentType == null) {
            handler.handle(new Either.Left<>("content-type  must be provided"));
            return;
        }
        JsonObject filter = new JsonObject()
                .put("content-type", contentType)
                .put("providerId", this.providerId);

        if (action != null) {
            filter.put("action", action);
        }
        MongoDb.getInstance().findOne(DISCOVER_COLLECTION, filter, event -> {
            if (Field.OK.equals(event.body().getString(Field.STATUS))) {
                JsonObject record = event.body();
                if (!record.containsKey("result")) {
                    handler.handle(new Either.Left<>("[WopiHelper@getActionUrl] Content-type doesn't match Libre Office Online capabilities"));
                    return;
                }

                try {
                    handler.handle(new Either.Right<>(ActionURL.parse(record.getJsonObject("result").getString("url"))));
                } catch (MalformedURLException e) {
                    log.error(e.getMessage());
                    handler.handle(new Either.Left<>("[WopiHelper@getActionUrl] Failed to parse action url"));
                }
            } else {
                handler.handle(new Either.Left<>(event.body().getString("message")));
            }
        });
    }

    /**
     * Discover Libre Office Online format capabilities
     *
     * @param handler Function handler returning data
     */
    public void discover(Wopi wopi, Handler<Boolean> handler) {
        String discoverUri = "/hosting/discovery";
        HttpClientRequest req = httpClient.get(discoverUri, response -> {
            if (response.statusCode() != 200) {
                log.error("[WopiHelper@discover] An error occurred when discovering wopi api.");
            } else {
                Buffer responseBuffer = new BufferImpl();
                response.handler(responseBuffer::appendBuffer);
                response.endHandler(aVoid -> parseDiscover(wopi, responseBuffer, handler));
                response.exceptionHandler(throwable -> {
                    log.error(throwable);
                    handler.handle(false);
                });
            }
        });

        req.end();
    }

    /**
     * Parse discover file
     *
     * @param buffer  discover file
     * @param handler Function handler returning data
     */
    private void parseDiscover(Wopi wopi, Buffer buffer, Handler<Boolean> handler) {
        JsonArray actions = wopi.provider().parseDiscovery(wopi.id(), buffer);
        MongoDb.getInstance().delete(DISCOVER_COLLECTION, new JsonObject().put("providerId", wopi.id()), MongoDbResult.validResultHandler(delete -> {
            if (delete.isLeft()) {
                handler.handle(Boolean.FALSE);
                return;
            }

            MongoDb.getInstance().insert(DISCOVER_COLLECTION, actions, MongoDbResult.validResultHandler(either -> handler.handle(either.isRight())));
        }));
    }

    /**
     * Encode string parameter as HTTP param using HttpHelper
     *
     * @param param parameter to encode
     * @return Parameter encoded as HTTP format
     */
    public String encodeWopiParam(String param) {
        return httpHelper.encode(param);
    }

    /**
     * Validate provided token. It check in token collection if token match session and file
     *
     * @param tokenId    Token identifier
     * @param documentId Document identifier
     * @param right      Right the user need. It should be a String from Binding enum.
     * @param handler    Function handler returning data
     */
    public void validateToken(String tokenId, String documentId, String right, Handler<JsonObject> handler) {
        QueryBuilder query = new QueryBuilder().and(
                QueryBuilder.start(Field._ID).is(tokenId).get(),
                QueryBuilder.start("document").is(documentId).get()
        );
        MongoDb.getInstance().findOne(TOKEN_COLLECTION, MongoQueryBuilder.build(query), message -> {
            Either<String, JsonObject> tokenEvent = Utils.validResult(message);
            if (tokenEvent.isRight()) {
                if (tokenEvent.right().getValue().size() == 0) {
                    handler.handle(new JsonObject().put("valid", false).put("err", "No token found"));
                    return;
                }
                Token token = new Token(tokenEvent.right().getValue());
                if (!token.isValid()) {
                    handler.handle(new JsonObject().put("valid", false).put(Field.TOKEN, tokenEvent.right().getValue()));
                    return;
                }
                UserUtils.getSession(eb, token.getSessionId(), session -> {
                    if (session == null || !token.getUser().equals(session.getString("userId"))) {
                        handler.handle(new JsonObject().put("valid", false).put("err", session == null ? "Session not found" : "Invalid user"));
                        return;
                    }
                    userCan(token.getSessionId(), documentId, right, can -> handler.handle(new JsonObject().put("valid", can).put(Field.TOKEN, tokenEvent.right().getValue())));
                });
            } else {
                handler.handle(new JsonObject().put("valid", false));
            }
        });
    }

    /**
     * Check if the user can read file based on given session identifier and given document identifier
     *
     * @param sessionId  Session identifier
     * @param documentId Document identifier
     * @param handler    Function handler returning data
     */
    public void userCanRead(String sessionId, String documentId, Handler<Boolean> handler) {
        userCan(sessionId, documentId, Bindings.READ.toString(), handler);
    }

    /**
     * Check if the user can write file based on given session identifier and given document identifier
     *
     * @param sessionId  Session identifier
     * @param documentId Document identifier
     * @param handler    Function handler returning data
     */
    public void userCanWrite(String sessionId, String documentId, Handler<Boolean> handler) {
        userCan(sessionId, documentId, Bindings.CONTRIB.toString(), handler);
    }

    /**
     * Check if the user can use the file. Verification is based on given session identifier, given document identifier and given right.
     *
     * @param sessionId  Session identifier
     * @param documentId Document identifier
     * @param right      Right needed
     * @param handler    Function handler returning data
     */
    private void userCan(String sessionId, String documentId, String right, Handler<Boolean> handler) {
        UserUtils.getSession(eb, sessionId, session -> {
            if (session == null) {
                handler.handle(false);
                return;
            }

            List<DBObject> groups = new ArrayList<>();
            groups.add(QueryBuilder.start("userId").is(session.getString("userId"))
                    .put(right).is(true).get());
            JsonArray groupsIds = session.getJsonArray("groupsIds");
            for (int i = 0; i < groupsIds.size(); i++) {
                String gpId = groupsIds.getString(i);
                groups.add(QueryBuilder.start("groupId").is(gpId)
                        .put(right).is(true).get());
            }
            QueryBuilder query = QueryBuilder.start(Field._ID).is(documentId).or(
                    QueryBuilder.start(Field.OWNER).is(session.getString("userId")).get(),
                    QueryBuilder.start("shared").elemMatch(
                            new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()).get(),
                    QueryBuilder.start("inheritedShares").elemMatch(
                            new QueryBuilder().or(groups.toArray(new DBObject[groups.size()])).get()).get()
            );

            MongoDb.getInstance().count(Field.DOCUMENTS, MongoQueryBuilder.build(query),
                    res -> handler.handle(res.body() != null && Field.OK.equals(res.body().getString(Field.STATUS)) && 1 == res.body().getInteger("count")));
        });
    }

    /**
     * Get Wopi provider file capabilities
     */
    public Future<JsonArray> getCapabilities() {
        Promise<JsonArray> promise = Promise.promise();
        JsonObject query = new JsonObject().put("providerId", this.providerId);
        JsonObject sort = new JsonObject();
        JsonObject keys = new JsonObject()
                .put("content-type", 1)
                .put("extension", 1)
                .put(Field._ID, 0);
        MongoDb.getInstance().find(DISCOVER_COLLECTION, query, sort, keys, event -> {
            if (Field.OK.equals(event.body().getString(Field.STATUS))) {
                promise.complete(event.body().getJsonArray("results"));
            } else {
                promise.fail(event.body().getString("message"));
            }
        });

        return promise.future();
    }

    /**
     * Clear all Lool tokens
     *
     * @param handler Function handler returning data
     */
    public void clearTokens(Handler<Either<String, JsonObject>> handler) {
        MongoDb.getInstance().delete(WopiHelper.TOKEN_COLLECTION, new JsonObject(), message -> handler.handle(Utils.validResult(message)));
    }

    /**
     * Check user is token owner on given document
     *
     * @param userId     User identifier
     * @param token      Access token
     * @param documentId Document identifier
     * @param handler    Function handler returning data
     */
    public void isUserToken(String userId, String token, String documentId, Handler<Boolean> handler) {
        JsonObject matcher = new JsonObject()
                .put("user", userId)
                .put("document", documentId)
                .put(Field._ID, token);

        MongoDb.getInstance().findOne(TOKEN_COLLECTION, matcher, message -> {
            Either<String, JsonObject> either = Utils.validResult(message);
            handler.handle(either.isRight() && either.right().getValue().containsKey(Field._ID));
        });
    }

    /**
     * Delete given token
     *
     * @param token   Token to delete
     * @param handler Function handler returning data
     */
    public void deleteToken(String token, Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject()
                .put(Field._ID, token);

        MongoDb.getInstance().delete(TOKEN_COLLECTION, matcher, message -> handler.handle(Utils.validResult(message)));
    }

    /**
     * Invalid given token
     *
     * @param token   Token to invalid
     * @param handler Function handler returning data
     */
    public void invalidateToken(String token, Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject()
                .put(Field._ID, token);
        MongoDb.getInstance().findOne(TOKEN_COLLECTION, matcher, message -> {
            Either<String, JsonObject> either = Utils.validResult(message);
            if (either.isLeft()) {
                handler.handle(new Either.Left<>("Failed to retrieve token"));
                return;
            }

            JsonObject object = either.right().getValue();
            object.put("valid", false);
            MongoDb.getInstance().update(TOKEN_COLLECTION, matcher, object, messageUpdate -> handler.handle(Utils.validResult(messageUpdate)));
        });
    }
}
