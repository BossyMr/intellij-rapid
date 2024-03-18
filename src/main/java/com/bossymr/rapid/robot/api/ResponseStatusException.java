package com.bossymr.rapid.robot.api;

import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * A {@code ResponseStatusException} indicates an unsuccessful response was received. The exception contains information
 * on both response and the request which caused the response.
 */
public class ResponseStatusException extends IOException {

    private final Response response;

    public ResponseStatusException(@NotNull Response response, @NotNull String message) {
        super(message);
        this.response = response;
    }

    public @NotNull Response getResponse() {
        return response;
    }

    public @NotNull Request getRequest() {
        return response.request();
    }

    @Override
    public @NotNull String toString() {
        int statusCode = getResponse().code();
        URI path = getRequest().url().uri();
        return "ResponseStatusException: " + statusCode + " " + path + " '" + getMessage() + "'";
    }
}
