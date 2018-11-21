package fr.openent.lool.service.Impl;

import com.mongodb.QueryBuilder;
import fr.openent.lool.Lool;
import fr.openent.lool.service.TokenService;
import fr.wseduc.mongodb.MongoDb;
import fr.wseduc.mongodb.MongoQueryBuilder;
import fr.wseduc.webutils.Either;
import fr.wseduc.webutils.Utils;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

public class DefaultTokenService implements TokenService {

    @Override
    public void get(String token, String document, Handler<Either<String, JsonObject>> handler) {
        QueryBuilder query = new QueryBuilder().and(
                QueryBuilder.start("_id").is(token).get(),
                QueryBuilder.start("document").is(document).get()
        );
        MongoDb.getInstance().findOne(Lool.TOKEN_COLLECTION, MongoQueryBuilder.build(query), message -> {
            handler.handle(Utils.validResult(message));
        });
    }
}
