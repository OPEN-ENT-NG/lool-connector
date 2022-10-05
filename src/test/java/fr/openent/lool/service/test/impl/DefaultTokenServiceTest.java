package fr.openent.lool.service.test.impl;


import fr.openent.lool.core.constants.Field;
import fr.openent.lool.service.TokenService;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.unit.Async;
import fr.openent.lool.Lool;
import fr.openent.lool.service.Impl.DefaultTokenService;
import fr.wseduc.mongodb.MongoDb;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.entcore.common.user.UserInfos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.print.ServiceUIFactory;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.Mockito.mock;

@RunWith(VertxUnitRunner.class)
public class DefaultTokenServiceTest {
    private Vertx vertx;
    private final MongoDb mongo = mock(MongoDb.class);
    private TokenService defaultTokenService;

    @Before
    public void setUp(TestContext ctx) {
        defaultTokenService = new DefaultTokenService();
        vertx = Vertx.vertx();
        MongoDb.getInstance().init(vertx.eventBus(), "fr.openent.lool");
    }

    @Test
    public void testCreate(TestContext ctx) {
        Async async = ctx.async();
        String expectedCollection = "document_token";
        JsonObject expectedParams = expectedJsonTestCreate();

        vertx.eventBus().consumer("fr.openent.lool", message -> {
            JsonObject body = (JsonObject) message.body();
            body.getJsonObject("document").remove(Field._ID);
            ctx.assertEquals(expectedCollection, body.getString("collection"));
            ctx.assertEquals(expectedParams, body.getJsonObject("document"));
            async.complete();
        });
        defaultTokenService.create("document_id", "user_id", null);
    }

    private JsonObject expectedJsonTestCreate() {
        return new JsonObject("{ " +
                "\"document\": \"document_id\", " +
                "\"user\": \"user_id\"" +
                "}");
    }

    @Test
    public void testGet(TestContext ctx) {
        Async async = ctx.async();
        String expectedCollection = "document_token";
        JsonObject expectedParams = expectedJsonTestGet();
        vertx.eventBus().consumer("fr.openent.lool", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(expectedCollection, body.getString("collection"));
            ctx.assertEquals(expectedParams, body.getJsonObject("matcher"));
            async.complete();
        });
        defaultTokenService.get(Field.ID, null);
    }

    private JsonObject expectedJsonTestGet() {
        return new JsonObject("{" +
                "\"_id\": \"id\"" +
                "}");
    }

    @Test
    public void testDelete(TestContext ctx) {
        Async async = ctx.async();
        String expectedCollection = "document_token";
        JsonObject expectedParams = new JsonObject().put(Field._ID, Field.ID);
        vertx.eventBus().consumer("fr.openent.lool", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(expectedCollection, body.getString("collection"));
            ctx.assertEquals(expectedParams, body.getJsonObject("matcher"));
            async.complete();
        });
        defaultTokenService.delete(Field.ID, null);
    }

    @Test
    public void testClean(TestContext ctx) {
        Async async = ctx.async();
        String expectedCollection = "document_token";
        vertx.eventBus().consumer("fr.openent.lool", message -> {
            JsonObject body = (JsonObject) message.body();
            ctx.assertEquals(expectedCollection, body.getString("collection"));
            async.complete();
        });
        defaultTokenService.clean(null);
    }
}