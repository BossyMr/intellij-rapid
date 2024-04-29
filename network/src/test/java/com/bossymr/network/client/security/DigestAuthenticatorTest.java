package com.bossymr.network.client.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigestAuthenticatorTest {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @Test
    void authenticate() throws IOException, InterruptedException {
        URI path = URI.create("http://httpbin.org/digest-auth/auth/" + USERNAME + "/" + PASSWORD);
        HttpClient httpClient = HttpClient.newHttpClient();
        DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(USERNAME, PASSWORD));
        HttpRequest request = HttpRequest.newBuilder(path).build();
        // Try to send the request without authentication
        HttpResponse<Void> unauthenticated = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        assertEquals(401, unauthenticated.statusCode());
        // Try to send the request with authentication
        HttpResponse<Void> authenticated = httpClient.send(authenticator.authenticate(unauthenticated), HttpResponse.BodyHandlers.discarding());
        assertEquals(200, authenticated.statusCode());
    }
}