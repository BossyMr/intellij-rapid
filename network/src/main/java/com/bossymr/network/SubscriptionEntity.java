package com.bossymr.network;

import com.bossymr.network.client.EntityModel;
import com.bossymr.network.client.NetworkAction;
import com.bossymr.network.client.SubscribableEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * A {@code SubscriptionEntity} represents an ongoing subscription.
 */
public abstract class SubscriptionEntity {

    private final @NotNull SubscribableEvent<?> event;
    private final @NotNull SubscriptionPriority priority;
    private final @NotNull NetworkAction networkAction;

    public SubscriptionEntity(@NotNull NetworkAction networkAction, @NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority) {
        this.networkAction = networkAction;
        this.event = event;
        this.priority = priority;
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

    public @NotNull NetworkAction getNetworkAction() {
        return networkAction;
    }

    /**
     * Unsubscribes from this subscription.
     */
    public void unsubscribe() throws IOException, InterruptedException {
        getNetworkAction().unsubscribe(List.of(this));
    }

    public static void unsubscribe(@NotNull Collection<SubscriptionEntity> entities) throws IOException, InterruptedException {
        Map<NetworkAction, List<SubscriptionEntity>> organized = new HashMap<>();
        for (SubscriptionEntity entity : entities) {
            organized.putIfAbsent(entity.getNetworkAction(), new ArrayList<>());
            organized.get(entity.getNetworkAction()).add(entity);
        }
        for (NetworkAction networkAction : organized.keySet()) {
            networkAction.unsubscribe(organized.get(networkAction));
        }
    }

    public abstract void event(@NotNull EntityModel model);

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "identity=" + Integer.toHexString(hashCode()) +
                ", event=" + event +
                ", priority=" + priority +
                '}';
    }
}
