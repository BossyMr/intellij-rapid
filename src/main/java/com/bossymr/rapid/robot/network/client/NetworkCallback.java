package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.network.ResponseStatusException;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.client.model.ModelFactory;
import com.bossymr.rapid.robot.network.client.model.Property;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public abstract class NetworkCallback<T> implements Callback {

    private final CompletableFuture<T> completableFuture;
    private final ModelFactory factory;

    public NetworkCallback(ModelFactory factory, CompletableFuture<T> completableFuture) {
        this.completableFuture = completableFuture;
        this.factory = factory;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
        completableFuture.completeExceptionally(e);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) {
        if(response.isSuccessful()) {
            try {
                onResponse(response);
            } catch (IOException e) {
                completableFuture.completeExceptionally(e);
            }
        } else {
            try {
                onFailure(response);
            } catch (IOException e) {
                completableFuture.completeExceptionally(e);
            }
        }
    }

    public abstract void onResponse(@NotNull Response response) throws IOException;

    public void onFailure(@NotNull Response response) throws IOException {
        ResponseBody responseBody = response.body();
        String contentType = response.header("Content-Type");
        if(contentType == null || responseBody == null) throw new ResponseStatusException(response.code());
        switch (contentType) {
            case "text/plain" -> completableFuture.completeExceptionally(new ResponseStatusException(responseBody.string(), response.code()));
            case "application/xhtml+xml" -> {
                Model model = factory.getModel(responseBody);
                int responseCode = model.entity().getProperty("code")
                        .map(Property::content)
                        .map(Integer::parseInt)
                        .orElseThrow();
                String message = model.entity().getProperty("msg")
                        .map(Property::content)
                        .orElseThrow();
                completableFuture.completeExceptionally(new ResponseStatusException(message, response.code(), responseCode));
            }
            default -> completableFuture.completeExceptionally(new ResponseStatusException(response.code()));
        }
    }
}
