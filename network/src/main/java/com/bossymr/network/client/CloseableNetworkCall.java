package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code CloseableNetworkCall} is a {@link NetworkCall} which, when closed, will automatically cancel all
 * asynchronous requests.
 *
 * @param <T> the type of respones body.
 */
public abstract class CloseableNetworkCall<T> implements NetworkCall<T> {

    private final @NotNull NetworkEngine networkEngine;

    protected CloseableNetworkCall(@NotNull NetworkEngine networkEngine) {
        this.networkEngine = networkEngine;
    }

    @Override
    public @Nullable T send() throws IOException, InterruptedException {
        return create();
    }

    @Override
    public @NotNull CompletableFuture<T> sendAsync() {
        CompletableFuture<T> request = createAsync();
        return networkEngine.track(request);
    }

    /**
     * This method should be overridden to synchronously send the request.
     *
     * @return the response.
     */
    protected abstract @Nullable T create() throws IOException, InterruptedException;

    /**
     * This method should be overridden to asynchronously send the request.
     *
     * @return the response.
     */
    protected abstract @NotNull CompletableFuture<T> createAsync();
}
