package com.bossymr.rapid.robot.network.client.impl;

import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class QueryImpl<T> implements Query<T> {

    private final HttpRequest request;
    private final NetworkClient networkClient;
    private final Type returnType;


    public QueryImpl(@NotNull NetworkClient networkClient, @NotNull HttpRequest request, @NotNull Type returnType) {
        this.networkClient = networkClient;
        this.request = request;
        this.returnType = returnType;
    }

    @Override
    public @Nullable T send() throws IOException {
        return networkClient.process(networkClient.send(request), returnType);
    }

    @Override
    public @NotNull CompletableFuture<T> sendAsync() {
        return networkClient.sendAsync(request)
                .thenComposeAsync(response -> {
                    try {
                        return networkClient.processAsync(response, returnType);
                    } catch (IOException e) {
                        throw new CompletionException(e);
                    }
                });
    }
}
