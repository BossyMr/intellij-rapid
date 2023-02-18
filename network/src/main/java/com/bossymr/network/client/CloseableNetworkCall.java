package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A {@code CloseableNetworkCall} is a {@link NetworkCall} which, when closed, will automatically cancel all
 * asynchronous requests.
 *
 * @param <T> the type of respones body.
 */
public abstract class CloseableNetworkCall<T> implements NetworkCall<T> {

    private final @NotNull Set<CompletableFuture<?>> requests = ConcurrentHashMap.newKeySet();

    private volatile boolean closed;

    public boolean isClosed() {
        return closed;
    }

    @Override
    public @Nullable T send() throws IOException, InterruptedException {
        if (closed) {
            throw new IllegalStateException("NetworkCall is closed");
        }
        return create();
    }

    @Override
    public @NotNull CompletableFuture<T> sendAsync() {
        if (closed) {
            throw new IllegalStateException("NetworkCall '" + this + "' is closed");
        }
        CompletableFuture<T> request = createAsync();
        requests.add(request);
        return request.handleAsync((response, throwable) -> {
            requests.remove(request);
            if (throwable != null) {
                throw HttpNetworkClient.getThrowable(throwable);
            }
            return response;
        });
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

    @Override
    public void close() {
        if (closed) {
            return;
        }
        for (CompletableFuture<?> request : requests) {
            request.cancel(false);
        }
        closed = true;
    }
}
