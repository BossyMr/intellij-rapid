package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

/**
 * A {@code SubscribableNetworkCall} represents a subscribable network resource.
 *
 * @param <T> the type of event body.
 */
public interface SubscribableNetworkCall<T> {

    /**
     * Subscribes to this resource with the specified priority. The specified callback will be called, for each event
     * received - it's {@code SubscriptionEntity} argument will be the same as the following endpoint, and is used to
     * close the endpoint.
     *
     * @param priority the subscription priority.
     * @param listener the subscription callback.
     * @return an entity used to unsubscribe.
     */
    @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener);

}
