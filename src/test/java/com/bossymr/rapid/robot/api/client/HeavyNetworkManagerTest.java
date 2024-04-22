package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.annotations.*;
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
            NetworkRequest<String> request = new NetworkRequest<>(URI.create("/"), GenericType.of(String.class));
            try (NetworkManager action = new NetworkAction(manager)) {
                NetworkQuery<String> query = action.createQuery(request);
                assertEquals("Hello, World!", assertDoesNotThrow(query::get));
            }
        }
    }

    @Test
    void modelQuery(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("Hello!", "entity")
                .setProperty("string", "Hello, World!")
                .setProperty("integer", "1")
                .setProperty("enum", "state")
                .build();
        ResponseModel model = ResponseModel.newBuilder()
                .setEntity(entity)
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", model.toText())));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            try (NetworkManager action = new NetworkAction(manager)) {
                NetworkQuery<ResponseModel> modelQuery = action.createQuery(new NetworkRequest<>(URI.create("/"), GenericType.of(ResponseModel.class)));
                assertEquals(model, assertDoesNotThrow(modelQuery::get));
                NetworkQuery<TestEntity> entityQuery = action.createQuery(new NetworkRequest<>(URI.create("/"), GenericType.of(TestEntity.class)));
                TestEntity testEntity = assertDoesNotThrow(entityQuery::get);
                assertEquals("Hello!", testEntity.getTitle());
                assertEquals("Hello, World!", testEntity.getProperty());
                assertEquals(TestEntity.State.STATE, testEntity.getState());
                assertEquals(1, testEntity.getInteger());
                assertNull(testEntity.getEmpty());
            }
        }
    }

    @Test
    void subtypeQuery(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("", "subtype")
                .setProperty("string", "Hello, World!")
                .setProperty("integer", "1")
                .setProperty("override", "2")
                .setProperty("custom", "Greetings, World!")
                .build();
        ResponseModel model = ResponseModel.newBuilder()
                .setEntity(entity)
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", model.toText())));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            try (NetworkManager action = new NetworkAction(manager)) {
                NetworkQuery<ResponseModel> modelQuery = action.createQuery(new NetworkRequest<>(URI.create("/"), GenericType.of(ResponseModel.class)));
                assertEquals(model, assertDoesNotThrow(modelQuery::get));
                assertInstanceOf(TestSubType.class, assertDoesNotThrow(() -> action.createQuery(new NetworkRequest<>(URI.create("/"), GenericType.of(TestEntity.class))).get()));
                NetworkQuery<TestSubType> entityQuery = action.createQuery(new NetworkRequest<>(URI.create("/"), GenericType.of(TestSubType.class)));
                TestSubType testEntity = assertDoesNotThrow(entityQuery::get);
                assertThrows(ProxyException.class, testEntity::getProperty);
                assertEquals("Greetings, World!", testEntity.getCustom());
                assertEquals(2, testEntity.getInteger());
            }
        }
    }

    @Test
    void modelFetch(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("", "entity")
                .setProperty("property", "/propertyPath")
                .setReference("self", URI.create("/selfPath"))
                .build();
        ResponseModel model = ResponseModel.newBuilder()
                .setEntity(entity)
                .build();
        wireMock.register(get("/").willReturn(ok("Hello, World!")));
        wireMock.register(post("/selfPath/request?argument=value&arguments=values")
                .willReturn(okForContentType("application/xhtml+xml", model.toText())));
        wireMock.register(put("/propertyPath/request")
                .willReturn(ok()));
        wireMock.register(delete("/failPath")
                .willReturn(badRequest()));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            NetworkRequest<TestFetch> request = new NetworkRequest<>(FetchMethod.POST, URI.create("/selfPath/request"), GenericType.of(TestFetch.class))
                    .setArgument("argument", "value")
                    .setArgument("arguments", "values");
            try (NetworkManager action = new NetworkAction(manager)) {
                NetworkQuery<TestFetch> modelQuery = action.createQuery(request);
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
                    assertEquals(400, e.getResponse().code());
                }
            }
        }
    }

    @Test
    void expandTest(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        ResponseModel completeModel = ResponseModel.newBuilder()
                .setEntity(EntityModel.newBuilder("Hello!", "entity")
                        .setProperty("string", "Hello, World!")
                        .setProperty("integer", "1")
                        .setProperty("enum", "state")
                        .setReference("self", URI.create(runtimeInfo.getHttpBaseUrl()).resolve("/complete"))
                        .build())
                .build();
        ResponseModel simpleModel = ResponseModel.newBuilder()
                .setEntity(EntityModel.newBuilder("Hello!", "entity-li")
                        .setProperty("string", "Hello, World!")
                        .setReference("self", URI.create(runtimeInfo.getHttpBaseUrl()).resolve("/complete"))
                        .build())
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", simpleModel.toText())));
        wireMock.register(get("/complete").willReturn(okForContentType("application/xhtml+xml", completeModel.toText())));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            try (NetworkManager action = new NetworkAction(manager)) {
                TestService service = action.createService(TestService.class);
                TestEntity entity = service.getEntity().get();
                EntityProxy proxy = assertInstanceOf(EntityProxy.class, entity);
                assertEquals("entity-li", proxy.getType());
                int events = wireMock.getServeEvents().size();
                entity.getProperty();
                assertEquals(events, wireMock.getServeEvents().size());
                assertEquals("entity-li", proxy.getType());
                entity.getInteger();
                assertEquals(events + 1, wireMock.getServeEvents().size());
                assertEquals("entity", proxy.getType());
            }
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

            @Deserializable("state")
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
        @Fetch(method = FetchMethod.GET, value = "/")
        NetworkQuery<String> withPath();

        @NotNull
        @Fetch(method = FetchMethod.POST, value = "{@self}/request", arguments = {"argument=value", "arguments=values"})
        NetworkQuery<ResponseModel> withArguments();

        @Fetch(method = FetchMethod.PUT, value = "{#property}/request")
        NetworkQuery<Void> withProperty();

        @Fetch(method = FetchMethod.DELETE, value = "/{path}")
        NetworkQuery<Void> fail(@NotNull @Path("path") String argument);

    }
}
