package com.bossymr.network.client;

import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.model.Model;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code NetworkClient} is used to send both synchronous and asynchronous requests, as well as to subscribe to an
 * event.
 */
public interface NetworkClient extends AutoCloseable {

    /**
     * Sends the specified request synchronously.
     *
     * @param request the request.
     * @return the response.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if this {@code NetworkClient} is interrupted.
     */
    @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) throws IOException, InterruptedException;

    /**
     * Sends the specified request asynchronously.
     *
     * @param request the request.
     * @return the response.
     */
    @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest request);

    /**
     * Subscribes to the specified event with the specified priority and callback.
     *
     * @param event the event.
     * @param priority the priority.
     * @param listener the listener.
     * @return an entity representing this subscription.
     */
    @NotNull SubscriptionEntity subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener);

    /**
     * Unsubscribes from the subscription associated with the specified entity.
     *
     * @param entity the entity.
     * @throws IllegalArgumentException if a subscription is not associated with the specified entity.
     */
    void unsubscribe(@NotNull SubscriptionEntity entity);

    /**
     * Closes this {@code NetworkClient} and unsubscribes from all ongoing subscriptions.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if this {@code NetworkClient} is interrupted.
     */
    @Override
    void close() throws IOException, InterruptedException;
}
