package com.bossymr.network.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code HttpNetworkCall} is a {@code NetworkCall} which sends requests through an {@link EntityFactory}.
 *
 * @param <T> the type of response body.
 */
public class HttpNetworkCall<T> extends CloseableNetworkCall<T> {

    private final @NotNull EntityFactory factory;
    private final @NotNull HttpRequest request;
    private final @NotNull Type returnType;

    /**
     * Creates a new {@code NetworkCall}, which will send the specified request to the specified {@code EntityFactory}
     * and convert it into the specified type.
     *
     * @param factory the entity factory.
     * @param request the request.
     * @param returnType the response type.
     */
    public HttpNetworkCall(@NotNull EntityFactory factory, @NotNull HttpRequest request, @NotNull Type returnType) {
        this.factory = factory;
        this.request = request;
        this.returnType = returnType;
    }

    @Override
    public @NotNull HttpRequest request() {
        return request;
    }

    @Override
    protected @Nullable T create() throws IOException, InterruptedException {
        return factory.convert(request, returnType);
    }

    @Override
    protected @NotNull CompletableFuture<T> createAsync() {
        return factory.convertAsync(request, returnType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpNetworkCall<?> that = (HttpNetworkCall<?>) o;
        return factory.equals(that.factory) && request.equals(that.request) && returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(factory, request, returnType);
    }

    @Override
    public String toString() {
        return "HttpNetworkCall{" +
                "factory=" + factory +
                ", request=" + request +
                ", returnType=" + returnType +
                '}';
    }
}
