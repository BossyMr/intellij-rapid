package com.bossymr.network.client;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.client.security.Credentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okForContentType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WireMockTest
class NetworkManagerTest {

    @Test
    void stringQuery(@NotNull WireMockRuntimeInfo runtimeInfo) {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(okForContentType("text/plain", "Hello, World!")));
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", "".toCharArray()));
        HttpRequest request = manager.getNetworkClient().createRequest()
                .setPath(URI.create("/"))
                .build();
        NetworkQuery<String> query = manager.createQuery(String.class, request);
        assertEquals("Hello, World!", assertDoesNotThrow(query::get));
    }

    @Test
    void modelQuery(@NotNull WireMockRuntimeInfo runtimeInfo) {
        WireMock wireMock = runtimeInfo.getWireMock();
        EntityModel entity = EntityModel.newBuilder("Hello!", "entity")
                .setProperty("property", "Hello, World!")
                .build();
        ResponseModel model = ResponseModel.newBuilder()
                .setEntity(entity)
                .build();
        wireMock.register(get("/").willReturn(okForContentType("application/xhtml+xml", model.toText())));
        NetworkManager manager = new NetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", "".toCharArray()));
        HttpRequest request = manager.getNetworkClient().createRequest()
                .setPath(URI.create("/"))
                .build();
        NetworkQuery<ResponseModel> modelQuery = manager.createQuery(ResponseModel.class, request);
        assertEquals(model, assertDoesNotThrow(modelQuery::get));
        NetworkQuery<ServiceTest> entityQuery = manager.createQuery(ServiceTest.class, request);
        ServiceTest serviceTest = assertDoesNotThrow(entityQuery::get);
        assertEquals("Hello!", serviceTest.getTitle());
        assertEquals("Hello, World!", serviceTest.getProperty());
    }

    @Entity("entity")
    public interface ServiceTest {

        @Property("title")
        @NotNull String getTitle();

        @Property("property")
        @NotNull String getProperty();

    }
}
