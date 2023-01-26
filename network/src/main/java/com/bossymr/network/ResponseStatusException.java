package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

/**
 * A {@code ResponseStatusException} indicates an unsuccessful response was received. The exception contains information
 * on both response and the request which caused the response.
 */
public class ResponseStatusException extends IOException {

    private final HttpResponse<byte[]> response;

    public ResponseStatusException(@NotNull HttpResponse<byte[]> response) {
        this.response = response;
    }

    public @NotNull HttpResponse<byte[]> getResponse() {
        return response;
    }

    public @NotNull HttpRequest getRequest() {
        return response.request();
    }

    @Override
    public @NotNull String getMessage() {
        return Arrays.toString(response.body());
    }

    @Override
    public @NotNull String toString() {
        int statusCode = getResponse().statusCode();
        URI path = getRequest().uri();
        return "ResponseStatusException: " + statusCode + " " + path + " '" + getMessage() + "'";
    }
}
