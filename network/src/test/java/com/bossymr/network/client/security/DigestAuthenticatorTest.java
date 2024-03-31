package com.bossymr.network.client.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DigestAuthenticatorTest {

    @Test
    void authenticate() throws IOException, InterruptedException {
        final String username = "username";
        final String password = "password";
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://httpbin.org/digest-auth/auth/" + username + "/" + password))
                                         .build();
        HttpResponse<Void> unauthenticated = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        assertEquals(401, unauthenticated.statusCode());
        DigestAuthenticator authenticator = new DigestAuthenticator(new Credentials(username, password));
        HttpResponse<Void> authenticated = httpClient.send(authenticator.authenticate(unauthenticated), HttpResponse.BodyHandlers.discarding());
        assertEquals(200, authenticated.statusCode());
    }
}