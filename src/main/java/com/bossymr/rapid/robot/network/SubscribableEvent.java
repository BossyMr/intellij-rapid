package com.bossymr.rapid.robot.network;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * A {@code SubscribableEvent} represents a subscribable event.
 *
 * @param <T>
 */
public class SubscribableEvent<T> {

    /**
     * Subscribes to this subscribable event, with the specified priority, with the specified callback to consume each
     * received event.
     *
     * @param priority the priority of the subscription.
     * @param onEntity the callback to consume received events.
     * @return the entity representing the created subscription.
     */
    public @NotNull SubscribableEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscribableEntity, T> onEntity) {
        return null;
    }

}
