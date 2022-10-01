package com.bossymr.rapid.network;

import org.jetbrains.annotations.NotNull;

/**
 * A {@code SubscriptionEntity} represents a subscription to a {@link SubscribableEvent}.
 */
public class SubscribableEntity {

    /**
     * Sends a request to unsubscribe from this subscription.
     *
     * @return a request.
     */
    public @NotNull NetworkQuery<Void> unsubscribe() {
        return null;
    }

}
