package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

public class HttpNetworkCall<T> implements NetworkCall<T> {

    private final @NotNull HttpRequest request;
    private final @NotNull HttpNetworkFactory networkFactory;
    private final @NotNull Type returnType;

    public HttpNetworkCall(@NotNull HttpRequest request, @NotNull Type returnType, @NotNull HttpNetworkFactory networkFactory) {
        this.request = request;
        this.returnType = returnType;
        this.networkFactory = networkFactory;
    }

    @Override
    public @Nullable T send() throws IOException, InterruptedException {
        return networkFactory.getEntityFactory().convert(request, returnType);
    }

    @Override
    public @NotNull CompletableFuture<T> sendAsync() {
        return networkFactory.getEntityFactory().convertAsync(request, returnType);
    }
}
