package com.bossymr.network.client;

import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * A {@code CloseableSubscribableNetworkCall} is a {@link SubscribableNetworkCall} which, when closed, will
 * automatically unsubscribe from all ongoing subscriptions.
 *
 * @param <T> the type of event body.
 */
public abstract class CloseableSubscribableNetworkCall<T> implements SubscribableNetworkCall<T> {

    private final @NotNull Set<SubscriptionEntity> entities = ConcurrentHashMap.newKeySet();

    private volatile boolean closed;

    public boolean isClosed() {
        return closed;
    }

    @Override
    public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener) {
        if (closed) {
            throw new IllegalStateException("SubscribableNetworkCall '" + this + "'is closed");
        }
        CompletableFuture<SubscriptionEntity> request = create(priority, new SubscriptionListener<>() {
            @Override
            public void onEvent(@NotNull SubscriptionEntity entity, @NotNull T event) {
                listener.onEvent(entity, event);
            }

            @Override
            public void onClose(@NotNull SubscriptionEntity entity) {
                listener.onClose(entity);
                entities.remove(entity);
            }
        });
        return request.handleAsync((response, throwable) -> {
            if (throwable != null) {
                throw HttpNetworkClient.getThrowable(throwable);
            }
            entities.add(response);
            return response;
        });
    }

    protected abstract @NotNull CompletableFuture<SubscriptionEntity> create(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener);

    @Override
    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        for (SubscriptionEntity entity : entities) {
            try {
                entity.unsubscribe().get();
            } catch (ExecutionException e) {
                throw new IOException(e);
            }
        }
        closed = true;
    }
}
