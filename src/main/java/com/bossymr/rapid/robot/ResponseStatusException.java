package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import com.bossymr.rapid.robot.network.client.model.ModelUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

/**
 * A {@code ResponseStatusException} indicates an unsuccessful response was received. The exception contains the
 * request, and the response.
 * <p>
 * If the response body is correctly formatted as an XHTML model, the error message and error code will automatically be
 * retrieved. In other cases, the response body, if available, is used as the error message, and the error code is not
 * available. The error name and description can be fetched with the path {@code /rw/retcode}.
 * <p>
 * An example of an unsuccessful response, with a response body containing the error message and error code:
 * <pre>{@code
 * <div class="status">
 *      <span class="code">[ERROR CODE]</span>
 *      <span class="msg">[ERROR MESSAGE]</span>
 * </div>
 * }</pre>
 */
public class ResponseStatusException extends IOException {

    private final HttpResponse<byte[]> response;

    private final CollectionModel collectionModel;

    /**
     * Creates a new {@code ResponseStatusException} originating with the specified response.
     *
     * @param response the response which was unsuccessful.
     * @throws IllegalArgumentException if the specified response was successful.
     */
    public ResponseStatusException(@NotNull HttpResponse<byte[]> response) throws IOException {
        this.response = response;
        if (response.statusCode() < 300) {
            throw new IllegalArgumentException();
        }
        this.collectionModel = getModel(response);
    }

    private static @Nullable CollectionModel getModel(@NotNull HttpResponse<byte[]> response) throws IOException {
        if (response.body().length > 0) {
            Optional<String> contentType = response.headers().firstValue("Content-Type");
            if (contentType.isPresent()) {
                if (contentType.orElseThrow().equals("application/xhtml+xml")) {
                    return ModelUtil.convert(response.body());
                }
            }
        }
        return null;
    }

    @Override
    public String getMessage() {
        if (collectionModel != null) {
            return collectionModel.getField("msg");
        }
        if (response.headers().firstValue("Content-Type").isEmpty()) {
            return null;
        }
        return new String(response.body());
    }

    /**
     * Returns the status code of the unsuccessful response.
     * <p>
     * Identical to {@code getResponse().statusCode()}
     *
     * @return the status code of the response.
     */
    public int getStatusCode() {
        return getResponse().statusCode();
    }

    /**
     * Returns the path of the request which caused the unsuccessful responses.
     * <p>
     * Identical to {@code getRequest().uri()}.
     *
     * @return the path of the request.
     */
    public @NotNull URI getPath() {
        return getRequest().uri();
    }

    /**
     * Returns the unsuccessful response.
     *
     * @return the unsuccessful response.
     */
    public @NotNull HttpResponse<byte[]> getResponse() {
        return response;
    }

    /**
     * Returns the response which caused the unsuccessful response.
     * <p>
     * Identical to {@code getResponse().request()}
     *
     * @return the response which caused the unsuccessful response.
     */
    public @NotNull HttpRequest getRequest() {
        return getResponse().request();
    }

    @Override
    public String toString() {
        String message = getMessage();
        return "ResponseStatusException" + " [" + (getStatusCode()) + "] " + getPath() + (message != null ? ": " + message : "");
    }
}
