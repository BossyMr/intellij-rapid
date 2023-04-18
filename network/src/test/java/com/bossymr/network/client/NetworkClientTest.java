package com.bossymr.network.client;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.security.Credentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@WireMockTest
public class NetworkClientTest {

    @Test
    void successful(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(ok("Hello, World!")));
        NetworkClient networkClient = new NetworkClient(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", "".toCharArray()));
        HttpRequest request = networkClient.createRequest()
                .setPath(URI.create("/"))
                .build();
        HttpResponse<byte[]> response = networkClient.send(request);
        assertEquals("Hello, World!", new String(response.body()));
    }

    @Test
    void unsuccessful(@NotNull WireMockRuntimeInfo runtimeInfo) {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/").willReturn(badRequest()));
        NetworkClient networkClient = new NetworkClient(URI.create(runtimeInfo.getHttpBaseUrl()), new Credentials("", "".toCharArray()));
        HttpRequest request = networkClient.createRequest()
                .setPath(URI.create("/"))
                .build();
        assertThrows(ResponseStatusException.class, () -> networkClient.send(request));
    }
}
