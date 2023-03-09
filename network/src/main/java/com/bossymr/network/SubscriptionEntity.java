package com.bossymr.network;

import com.bossymr.network.client.NetworkClient;
import com.bossymr.network.client.SubscribableEvent;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code SubscriptionEntity} represents an ongoing subscription.
 */
public class SubscriptionEntity {

    private final NetworkClient networkClient;
    private final SubscribableEvent<?> event;
    private final SubscriptionPriority priority;

    private final SubscriptionListener<Model> listener;

    public SubscriptionEntity(@NotNull NetworkClient networkClient, @NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener) {
        this.networkClient = networkClient;
        this.event = event;
        this.priority = priority;
        this.listener = listener;
    }

    /**
     * Returns the event to which this entity is subscribed.
     *
     * @return the event to which this entity is subscribed.
     */
    public @NotNull SubscribableEvent<?> getEvent() {
        return event;
    }

    /**
     * Returns the priority with which this entity is subscribed.
     *
     * @return the priority with which this entity is subscribed.
     */
    public @NotNull SubscriptionPriority getPriority() {
        return priority;
    }

    /**
     * This method is called for each retrieved model.
     *
     * @param model the event.
     */
    public void onEvent(@NotNull Model model) {
        listener.onEvent(this, model);
    }

    /**
     * Unsubscribes from this subscription.
     *
     * @return the request.
     */
    public CompletableFuture<Void> unsubscribe() {
        return networkClient.unsubscribe(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionEntity entity = (SubscriptionEntity) o;
        return Objects.equals(networkClient, entity.networkClient) && Objects.equals(getEvent(), entity.getEvent()) && getPriority() == entity.getPriority() && Objects.equals(listener, entity.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkClient, getEvent(), getPriority(), listener);
    }

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "event=" + event +
                ", priority=" + priority +
                '}';
    }
}
