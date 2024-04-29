package com.bossymr.network.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ResponseStatusException extends IOException {

    private final HttpResponse<byte[]> response;

    public ResponseStatusException(HttpResponse<byte[]> response) {
        this.response = response;
    }

    public @NotNull HttpResponse<byte[]> getResponse() {
        return response;
    }

    public @NotNull HttpRequest getRequest() {
        return response.request();
    }

    public @NotNull String getMessage() {
        // TODO: Try to parse the body as an XML object. If that fails, return the entire body content.
        byte[] content = getResponse().body();
        return new String(content);
    }

    @Override
    public @NotNull String toString() {
        int statusCode = response.statusCode();
        URI path = getRequest().uri();
        return "ResponseStatusException: " + statusCode + " " + path + " '" + getMessage() + "'";
    }
}
