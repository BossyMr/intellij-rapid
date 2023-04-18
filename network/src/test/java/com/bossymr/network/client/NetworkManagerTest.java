package com.bossymr.network.client;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.annotations.*;
import com.bossymr.network.client.proxy.EntityProxy;
import com.bossymr.network.client.proxy.ProxyException;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class NetworkManagerTest {

    @Test
    void stringQuery(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(okForContentType("text/plain", "Hello, World!")));
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null);
        Request request = manager.getNetworkClient().createRequest()
                .setPath(URI.create("/"))
                .build();
        try (NetworkAction action = manager.createAction()) {
            NetworkQuery<String> query = action.createQuery(String.class, request);
            assertEquals("Hello, World!", assertDoesNotThrow(query::get));
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
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null);
        Request request = manager.getNetworkClient().createRequest()
                .setPath(URI.create("/"))
                .build();
        try (NetworkAction action = manager.createAction()) {
            NetworkQuery<ResponseModel> modelQuery = action.createQuery(ResponseModel.class, request);
            assertEquals(model, assertDoesNotThrow(modelQuery::get));
            NetworkQuery<TestEntity> entityQuery = action.createQuery(TestEntity.class, request);
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
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null);
        Request request = manager.getNetworkClient().createRequest()
                .setPath(URI.create("/"))
                .build();
        try (NetworkAction action = manager.createAction()) {
            NetworkQuery<ResponseModel> modelQuery = action.createQuery(ResponseModel.class, request);
            assertEquals(model, assertDoesNotThrow(modelQuery::get));
            assertInstanceOf(TestSubType.class, assertDoesNotThrow(() -> action.createQuery(TestEntity.class, request).get()));
            NetworkQuery<TestSubType> entityQuery = action.createQuery(TestSubType.class, request);
            TestSubType testEntity = assertDoesNotThrow(entityQuery::get);
            assertThrows(ProxyException.class, testEntity::getProperty);
            assertEquals("Greetings, World!", testEntity.getCustom());
            assertEquals(2, testEntity.getInteger());
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
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null);
        Request request = manager.getNetworkClient().createRequest()
                .setMethod("POST")
                .setPath(URI.create("/selfPath/request"))
                .setArgument("argument", "value")
                .setArgument("arguments", "values")
                .build();
        try (NetworkAction action = manager.createAction()) {
            NetworkQuery<TestFetch> modelQuery = action.createQuery(TestFetch.class, request);
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
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null);
        try (NetworkAction action = manager.createAction()) {
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
