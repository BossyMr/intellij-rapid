package com.bossymr.network.client;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * A {@code SubscribableEvent} represents a subscribable endpoint.
 *
 * @param <T> the type of event.
 * @see NetworkFactory#createSubscribableNetworkCall(SubscribableEvent)
 */
public class SubscribableEvent<T> {

    private final @NotNull URI resource;
    private final @NotNull Class<T> eventType;

    /**
     * Creates a new {@code SubscribableEvent} with the specified resource and class.
     *
     * @param resource the path to the subscribable resource.
     * @param eventType the class of the event type.
     */
    public SubscribableEvent(@NotNull URI resource, @NotNull Class<T> eventType) {
        this.resource = resource;
        this.eventType = eventType;
    }

    /**
     * Returns the path to the subscribable resource.
     *
     * @return the path to the subscribable resource.
     */
    @NotNull
    public URI getResource() {
        return resource;
    }

    /**
     * Returns the class of the event type.
     *
     * @return the class of the event type.
     */
    @NotNull
    public Class<T> getEventType() {
        return eventType;
    }
}
