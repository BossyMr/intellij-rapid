package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code SubscribableNetworkCall} represents a subscribable network resource.
 *
 * @param <T> the type of event body.
 */
public interface SubscribableNetworkCall<T> extends AutoCloseable {

    /**
     * Subscribes to this resource with the specified priority. The specified callback will be called, for each event
     * received - it's {@code SubscriptionEntity} argument will be the same as the following endpoint, and is used to
     * close the endpoint.
     *
     * @param priority the subscription priority.
     * @param listener the subscription callback.
     * @return an entity used to unsubscribe.
     */
    @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener);

    /**
     * Closes this {@code SubscribableNetworkCall} and unsubscribes all ongoing subscriptions.
     *
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if this {@code SubscribableNetworkCall} is unsubscribed.
     */
    @Override
    void close() throws IOException, InterruptedException;
}
