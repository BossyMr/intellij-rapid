package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.RequestMethod;
import com.bossymr.rapid.robot.api.ResponseStatusException;
import com.bossymr.rapid.robot.api.client.security.Credentials;
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

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@WireMockTest
class NetworkClientTest {

    @Test
    void successful(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(ok("Hello, World!")));
        NetworkClient client = new NetworkClient(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", ""));
        NetworkQuery<HttpResponse<byte[]>> query = client.newRequest(RequestMethod.GET, URI.create("/")).build();
        HttpResponse<byte[]> response = query.get();
        assertEquals("Hello, World!", new String(response.body()));
    }

    @Test
    void unsuccessful(@NotNull WireMockRuntimeInfo runtimeInfo) {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(WireMock.status(321).withResponseBody(Body.ofBinaryOrText("Hello, World!".getBytes(), ContentTypeHeader.absent()))));
        NetworkClient client = new NetworkClient(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", ""));
        NetworkQuery<HttpResponse<byte[]>> query = client.newRequest(RequestMethod.GET, URI.create("/")).build();
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