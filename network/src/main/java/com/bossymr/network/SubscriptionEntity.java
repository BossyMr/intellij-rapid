package com.bossymr.network;

import com.bossymr.network.client.NetworkClient;
import com.bossymr.network.client.SubscribableEvent;
import com.bossymr.network.client.model.Model;
import org.jetbrains.annotations.NotNull;

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
    public SubscribableEvent<?> getEvent() {
        return event;
    }

    /**
     * Returns the priority with which this entity is subscribed.
     *
     * @return the priority with which this entity is subscribed.
     */
    public SubscriptionPriority getPriority() {
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
     */
    public void unsubscribe() {
        networkClient.unsubscribe(this);
    }
}
