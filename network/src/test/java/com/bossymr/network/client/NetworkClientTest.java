package com.bossymr.network.client;

import com.bossymr.network.GenericType;
import com.bossymr.network.client.security.Credentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Body;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class NetworkClientTest {

    @Test
    void successful(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(ok("Hello, World!")));
        NetworkClient client = new NetworkClient(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", ""));
        RawNetworkQuery<String> query = new RawNetworkQuery<>(client, RequestMethod.GET, URI.create("/"), GenericType.of(String.class));
        HttpResponse<byte[]> response = query.get();
        assertEquals("Hello, World!", new String(response.body()));
    }

    @Test
    void unsuccessful(@NotNull WireMockRuntimeInfo runtimeInfo) {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(WireMock.status(321).withResponseBody(Body.ofBinaryOrText("Hello, World!".getBytes(), ContentTypeHeader.absent()))));
        NetworkClient client = new NetworkClient(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", ""));
        RawNetworkQuery<String> query = new RawNetworkQuery<>(client, RequestMethod.GET, URI.create("/"), GenericType.of(String.class));
        try {
            query.get();
            fail();
        } catch (ResponseStatusException e) {
            assertEquals(321, e.getResponse().statusCode());
            assertEquals("Hello, World!", new String(e.getResponse().body()));
        } catch (Exception e) {
            fail();
        }
    }
}