package com.bossymr.network;

import com.bossymr.network.client.EntityModel;
import com.bossymr.network.client.SubscribableEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A {@code SubscriptionEntity} represents an ongoing subscription.
 */
public abstract class SubscriptionEntity {

    private final SubscribableEvent<?> event;
    private final SubscriptionPriority priority;

    public SubscriptionEntity(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority) {
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

    /**
     * Unsubscribes from this subscription.
     */
    public abstract void unsubscribe() throws IOException, InterruptedException;

    public abstract void event(@NotNull EntityModel model);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscriptionEntity that = (SubscriptionEntity) o;

        if (!event.equals(that.event)) return false;
        return priority == that.priority;
    }

    @Override
    public int hashCode() {
        int result = event.hashCode();
        result = 31 * result + priority.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "event=" + event +
                ", priority=" + priority +
                '}';
    }
}
