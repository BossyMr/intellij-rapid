package com.bossymr.network.client;

import com.bossymr.network.client.security.Authenticator;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.client.security.DigestAuthenticator;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NetworkClient {

    private final Authenticator authenticator;

    private final HttpClient client;
    private final URI basePath;

    public NetworkClient(@NotNull URI basePath, @NotNull Credentials credentials) {
        this.basePath = basePath;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
        this.authenticator = new DigestAuthenticator(credentials);
    }

    public @NotNull URI getBasePath() {
        return basePath;
    }

    public @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = send(request, HttpResponse.BodyHandlers.ofByteArray());
        if(response.statusCode() >= 300) {
            throw new ResponseStatusException(response);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> send(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        // Try to preemptively authenticate the request
        request = authenticate(request);
        HttpResponse<T> response = client.send(request, bodyHandler);
        // Check if the request needs to be authenticated
        if (response.statusCode() != 401 && response.statusCode() != 407) {
            return response;
        }
        // Try to reauthenticate the request
        request = authenticator.authenticate(response);
        if (request != null) {
            // The request could be authenticated
            return client.send(request, bodyHandler);
        }
        return response;
    }

    private @NotNull HttpRequest authenticate(@NotNull HttpRequest request) {
        HttpRequest httpRequest = authenticator.authenticate(request);
        return httpRequest != null ? httpRequest : request;
    }
}
