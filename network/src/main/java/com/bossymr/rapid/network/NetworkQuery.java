package com.bossymr.rapid.network;

import com.bossymr.rapid.network.client.model.Model;
import com.bossymr.rapid.network.client.model.ModelUtil;
import com.bossymr.rapid.network.client.model.Property;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * A {@code NetworkQuery} represents a query to a remote resource.
 *
 * @param <T> the response type of the query.
 */
public class NetworkQuery<T> {

    private final URI path;
    private final Call call;
    private final Function<Model, T> converter;

    public NetworkQuery(@NotNull URI path, @NotNull Call call, @Nullable Function<Model, T> converter) {
        this.path = path;
        this.call = call;
        this.converter = converter;
    }

    /**
     * Sends a request synchronously and processes the response.
     *
     * @return the response.
     */
    public T send() throws IOException {
        try (Response response = call.execute()) {
            return onResponse(response);
        }
    }

    /**
     * Sends the request asynchronously and processes the response.
     *
     * @return the asynchronous response.
     */
    public @NotNull CompletableFuture<T> sendAsync() {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                completableFuture.completeExceptionally(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try {
                    T value = NetworkQuery.this.onResponse(response);
                    completableFuture.complete(value);
                } catch (ResponseStatusException | IOException e) {
                    completableFuture.completeExceptionally(e);
                }
            }
        });
        return completableFuture;
    }

    private T onResponse(@NotNull Response response) throws IOException {
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null && responseBody.contentLength() > 0) {
                if(converter == null) {
                    throw new IllegalStateException();
                }
                Model model = ModelUtil.createModel(path, responseBody.byteStream());
                return converter.apply(model);
            } else {
                if (converter != null) {
                    throw new IllegalStateException();
                }
                return null;
            }
        } else {
            onFailure(response);
            throw new IllegalStateException();
        }
    }

    private void onFailure(@NotNull Response response) throws IOException {
        ResponseBody responseBody = response.body();
        String contentType = response.header("Content-Type");
        if(contentType == null || responseBody == null) throw new ResponseStatusException(response.code());
        switch (contentType) {
            case "text/plain" -> throw new ResponseStatusException(responseBody.string(), response.code());
            case "application/xhtml+xml" -> {
                Model model = ModelUtil.createModel(path, responseBody.byteStream());
                int responseCode = model.entity().getProperty("code")
                        .map(Property::content)
                        .map(Integer::parseInt)
                        .orElseThrow();
                String message = model.entity().getProperty("msg")
                        .map(Property::content)
                        .orElseThrow();
                throw new ResponseStatusException(message, response.code(), responseCode);
            }
            default -> throw new ResponseStatusException(response.code());
        }
    }
}
