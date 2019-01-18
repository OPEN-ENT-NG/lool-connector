package fr.openent.lool.service.Impl;

import fr.openent.lool.service.TokenService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Utils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.UUID;

public class DefaultTokenService implements TokenService {
    private final Logger log = LoggerFactory.getLogger(DefaultTokenService.class);
    private String TOKEN_COLLECTION = "document_token";

    @Override
    public void create(String documentId, String userId, Handler<Either<String, JsonObject>> handler) {
        JsonObject token = new JsonObject()
                .put("_id", UUID.randomUUID().toString())
                .put("document", documentId)
                .put("user", userId);

        MongoDb.getInstance().insert(TOKEN_COLLECTION, token, message -> {
            Either<String, JsonObject> either = Utils.validResult(message);
            if (either.isRight()) {
                handler.handle(new Either.Right<>(token));
            } else {
                String err = "[DefaultTokenService@create] Failed to create document token";
                log.error(err);
                handler.handle(new Either.Left<>(err));
            }
        });
    }

    @Override
    public void get(String id, Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject()
                .put("_id", id);

        MongoDb.getInstance().findOne(TOKEN_COLLECTION, matcher, message -> handler.handle(Utils.validResult(message)));
    }

    @Override
    public void delete(String id, Handler<Either<String, JsonObject>> handler) {
        JsonObject matcher = new JsonObject()
                .put("_id", id);

        MongoDb.getInstance().delete(TOKEN_COLLECTION, matcher, message -> handler.handle(Utils.validResult(message)));
    }
}
