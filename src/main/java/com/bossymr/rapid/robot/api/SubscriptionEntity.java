package com.bossymr.rapid.robot.api;

import com.bossymr.rapid.robot.api.client.NetworkClient;
import com.bossymr.rapid.robot.api.client.SubscribableEvent;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;

/**
 * A {@code SubscriptionEntity} represents an ongoing subscription.
 */
public abstract class SubscriptionEntity {

    private final @NotNull SubscribableEvent<?> event;
    private final @NotNull SubscriptionPriority priority;
    private final @NotNull NetworkClient client;

    /**
     * Creates a new {@code SubscriptionEntity}.
     *
     * @param client the client managing the subscription.
     * @param event the event which is subscribed to.
     * @param priority the subscription priority.
     */
    public SubscriptionEntity(@NotNull NetworkClient client, @NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority) {
        this.client = client;
        this.event = event;
        this.priority = priority;
    }

    /**
     * Unsubscribes all specified entities simultaneously.
     *
     * @param entities the entities to unsubscribe.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public static void unsubscribe(@NotNull Collection<SubscriptionEntity> entities) throws IOException, InterruptedException {
        MultiMap<NetworkClient, SubscriptionEntity> clients = new MultiMap<>();
        for (SubscriptionEntity entity : entities) {
            clients.put(entity.client, entity);
        }
        for (NetworkClient client : clients.keySet()) {
            client.unsubscribe(clients.getAll(client));
        }
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
     * Unsubscribes from this subscription.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public abstract void unsubscribe() throws IOException, InterruptedException;

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
