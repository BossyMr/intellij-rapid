package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.bossymr.rapid.robot.api.client.proxy.EntityProxy;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class HeavyNetworkManagerTest {

    @Test
    void stringQuery(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(okForContentType("text/plain", "Hello, World!")));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            NetworkTarget<String> target = NetworkTarget.newTarget(URI.create("/"), NetworkType.stringType()).build();
            NetworkQuery<String> query = manager.createQuery(target);
            assertEquals("Hello, World!", assertDoesNotThrow(query::get));
        }
    }

    @Test
    void modelQuery(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("entity", "Hello!")
                .property("string", "Hello, World!")
                .property("integer", "1")
                .property("enum", "state")
                .build();
        ResponseModel model = ResponseModel.newBuilder("", "")
                .entity(entity)
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", model.toXML())));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            manager.createQuery(NetworkTarget.newTarget(URI.create("/"), NetworkType.modelType()).build());
            NetworkQuery<ResponseModel> modelQuery = manager.createQuery(NetworkTarget.newTarget(URI.create("/"), NetworkType.modelType()).build());
            assertEquals(model, assertDoesNotThrow(modelQuery::get));
            NetworkQuery<TestEntity> entityQuery = manager.createQuery(NetworkTarget.newTarget(URI.create("/"), NetworkType.entityType(TestEntity.class)).build());
            TestEntity testEntity = assertDoesNotThrow(entityQuery::get);
            assertEquals("Hello!", testEntity.getTitle());
            assertEquals("Hello, World!", testEntity.getProperty());
            assertEquals(TestEntity.State.STATE, testEntity.getState());
            assertEquals(1, testEntity.getInteger());
            assertNull(testEntity.getEmpty());
        }
    }

    @Test
    void subtypeQuery(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("subtype", "")
                .property("string", "Hello, World!")
                .property("integer", "1")
                .property("override", "2")
                .property("custom", "Greetings, World!")
                .build();
        ResponseModel model = ResponseModel.newBuilder("", "")
                .entity(entity)
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", model.toXML())));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            NetworkQuery<ResponseModel> modelQuery = manager.createQuery(NetworkTarget.newTarget(URI.create("/"), NetworkType.modelType()).build());
            assertEquals(model, assertDoesNotThrow(modelQuery::get));
            assertInstanceOf(TestSubType.class, assertDoesNotThrow(() -> manager.createQuery(NetworkTarget.newTarget(URI.create("/"), NetworkType.entityType(TestEntity.class)).build())).get());
            NetworkQuery<TestSubType> entityQuery = manager.createQuery(NetworkTarget.newTarget(URI.create("/"), NetworkType.entityType(TestSubType.class)).build());
            TestSubType testEntity = assertDoesNotThrow(entityQuery::get);
            assertThrows(ProxyException.class, testEntity::getProperty);
            assertEquals("Greetings, World!", testEntity.getCustom());
            assertEquals(2, testEntity.getInteger());
        }
    }

    @Test
    void modelFetch(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("entity", "")
                .property("property", "/propertyPath")
                .link("self", URI.create("/selfPath"))
                .build();
        ResponseModel model = ResponseModel.newBuilder("", "")
                .entity(entity)
                .build();
        wireMock.register(get("/").willReturn(ok("Hello, World!")));
        wireMock.register(post("/selfPath/request?argument=value&arguments=values")
                .willReturn(okForContentType("application/xhtml+xml", model.toXML())));
        wireMock.register(put("/propertyPath/request")
                .willReturn(ok()));
        wireMock.register(delete("/failPath")
                .willReturn(badRequest()));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            NetworkTarget<TestFetch> request = NetworkTarget.newTarget(RequestMethod.POST, URI.create("/selfPath/request"), NetworkType.entityType(TestFetch.class))
                    .argument("argument", "value")
                    .argument("arguments", "values")
                    .build();
            NetworkQuery<TestFetch> modelQuery = manager.createQuery(request);
            TestFetch testFetch = modelQuery.get();
            assertNotNull(testFetch);
            assertEquals("/selfPath", testFetch.getSelf());
            assertEquals("Hello, World!", testFetch.withPath().get());
            assertEquals(model, testFetch.withArguments().get());
            testFetch.withProperty().get();
            try {
                testFetch.fail("failPath").get();
                fail();
            } catch (ResponseStatusException e) {
                assertEquals(400, e.getResponse().statusCode());
            }
        }
    }

    @Test
    void expandTest(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        ResponseModel completeModel = ResponseModel.newBuilder("", "")
                .entity("entity", "Hello!", builder -> builder
                        .property("string", "Hello, World!")
                        .property("integer", "1")
                        .property("enum", "state")
                        .link("self", URI.create(runtimeInfo.getHttpBaseUrl()).resolve("/complete"))
                        .build())
                .build();
        ResponseModel simpleModel = ResponseModel.newBuilder("", "")
                .entity("entity-li", "Hello!", builder -> builder
                        .property("string", "Hello, World!")
                        .link("self", URI.create(runtimeInfo.getHttpBaseUrl()).resolve("/complete"))
                        .build())
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", simpleModel.toXML())));
        wireMock.register(get("/complete").willReturn(okForContentType("application/xhtml+xml", completeModel.toXML())));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            TestService service = manager.createService(TestService.class);
            TestEntity entity = service.getEntity().get();
            EntityProxy proxy = assertInstanceOf(EntityProxy.class, entity);
            assertEquals("entity-li", proxy.getModel().getType());
            int events = wireMock.getServeEvents().size();
            entity.getProperty();
            assertEquals(events, wireMock.getServeEvents().size());
            assertEquals("entity-li", proxy.getModel().getType());
            entity.getInteger();
            assertEquals(events + 1, wireMock.getServeEvents().size());
            assertEquals("entity", proxy.getModel().getType());
        }
    }

    @Service
    public interface TestService {

        @NotNull
        @Fetch("/")
        NetworkQuery<TestEntity> getEntity();

    }

    @Entity(value = "entity",
            subtype = {
                    TestSubType.class
            })
    public interface TestEntity {

        @Title
        @NotNull String getTitle();

        @Property("string")
        @NotNull String getProperty();

        @Property("integer")
        int getInteger();

        @Property("enum")
        @NotNull State getState();

        @Property("empty")
        @Nullable State getEmpty();

        enum State {

            @Alias("state")
            STATE,

        }

    }

    @Entity("subtype")
    public interface TestSubType extends TestEntity {

        @Override
        @NotNull String getProperty();

        @Property("override")
        @Override
        int getInteger();

        @Property("custom")
        @NotNull String getCustom();

    }

    @Entity("entity")
    public interface TestFetch {

        @Property("{@self}")
        @NotNull String getSelf();

        @NotNull
        @Fetch(method = RequestMethod.GET, value = "/")
        NetworkQuery<String> withPath();

        @NotNull
        @Fetch(method = RequestMethod.POST, value = "{@self}/request", arguments = {"argument=value", "arguments=values"})
        NetworkQuery<ResponseModel> withArguments();

        @Fetch(method = RequestMethod.PUT, value = "{#property}/request")
        NetworkQuery<Void> withProperty();

        @Fetch(method = RequestMethod.DELETE, value = "/{path}")
        NetworkQuery<Void> fail(@NotNull @Path("path") String argument);

    }
}
