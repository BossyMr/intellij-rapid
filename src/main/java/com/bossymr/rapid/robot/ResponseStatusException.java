package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Objects;

/**
 * A {@code ResponseStatusException} indicates that an unsuccessful response was received.
 */
public class ResponseStatusException extends IOException {

    private final int statusCode;
    private final URI path;

    private final int errorCode;

    public ResponseStatusException(int statusCode, @NotNull URI path, @NotNull String message) {
        super(message);
        this.statusCode = statusCode;
        this.path = path;
        this.errorCode = 0;
    }

    public ResponseStatusException(int statusCode, @NotNull URI path, int errorCode, @NotNull String message) {
        super(message);
        this.statusCode = statusCode;
        this.path = path;
        this.errorCode = errorCode;
    }

    public static @NotNull ResponseStatusException of(@NotNull HttpResponse<byte[]> response) throws IOException {
        int statusCode = response.statusCode();
        URI path = response.uri();
        String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse(null);
        if ("application/xhtml+xml".equals(contentType)) {
            CollectionModel model = CollectionModel.getModel(response.body());
            int errorCode = Integer.parseInt(Objects.requireNonNull(model.model().field("code")));
            String message = Objects.requireNonNull(model.model().field("msg"));
            return new ResponseStatusException(statusCode, path, errorCode, message);
        }
        byte[] body = response.body();
        String content = new String(body);
        return new ResponseStatusException(statusCode, path, content);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public @NotNull URI getPath() {
        return path;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public @NotNull String toString() {
        String status = "[" + getStatusCode() + " " + getPath() + "]";
        String errorCode = getErrorCode() != 0 ? getErrorCode() + " " : "";
        return getClass().getName() + ": " + status + " " + errorCode + getMessage();
    }
}
