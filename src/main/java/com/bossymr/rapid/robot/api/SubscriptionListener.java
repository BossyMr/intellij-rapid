package com.bossymr.rapid.robot.api;

import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

/**
 * An {@code SubscriptionListener} is bound to a specific subscription, and is called for each event received.
 *
 * @param <T> the type of event.
 */
@FunctionalInterface
public interface SubscriptionListener<T> extends EventListener {

    /**
     * This method is called for each received event.
     *
     * @param entity the subscription.
     * @param event the event.
     */
    void onEvent(@NotNull SubscriptionEntity entity, @NotNull T event);

    /**
     * This method is called when this subscription is closed.
     *
     * @param entity the subscription.
     */
    default void onClose(@NotNull SubscriptionEntity entity) {}

}