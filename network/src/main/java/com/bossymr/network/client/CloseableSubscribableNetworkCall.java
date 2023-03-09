package com.bossymr.network.client;

import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A {@code CloseableSubscribableNetworkCall} is a {@link SubscribableNetworkCall} which, when closed, will
 * automatically unsubscribe from all ongoing subscriptions.
 *
 * @param <T> the type of event body.
 */
public abstract class CloseableSubscribableNetworkCall<T> implements SubscribableNetworkCall<T> {

    private final @NotNull NetworkEngine networkEngine;

    protected CloseableSubscribableNetworkCall(@NotNull NetworkEngine networkEngine) {
        this.networkEngine = networkEngine;
    }

    @Override
    public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener) {
        CompletableFuture<SubscriptionEntity> request = create(priority, new SubscriptionListener<>() {
            @Override
            public void onEvent(@NotNull SubscriptionEntity entity, @NotNull T event) {
                listener.onEvent(entity, event);
            }

            @Override
            public void onClose(@NotNull SubscriptionEntity entity) {
                listener.onClose(entity);
                networkEngine.untrack(entity);
            }
        });
        return request.handleAsync((response, throwable) -> {
            if (throwable != null) {
                throw HttpNetworkClient.getThrowable(throwable);
            }
            networkEngine.track(response);
            return response;
        });
    }

    protected abstract @NotNull CompletableFuture<SubscriptionEntity> create(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener);
}
