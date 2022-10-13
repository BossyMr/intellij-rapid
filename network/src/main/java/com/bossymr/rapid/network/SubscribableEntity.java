package com.bossymr.rapid.network;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A {@code SubscriptionEntity} represents a subscription to a {@link SubscribableEvent}.
 */
public class SubscribableEntity {

    /**
     * Sends a request to unsubscribe from this subscription.
     *
     * @return a request.
     */
    public @NotNull CompletableFuture<Void> unsubscribe() {
        return null;
    }

}
