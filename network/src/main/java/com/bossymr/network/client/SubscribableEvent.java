package com.bossymr.network.client;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * A {@code SubscribableEvent} represents a subscribable endpoint.
 *
 * @param <T> the type of event.
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
        try {
            this.resource = new URI(resource.getScheme(), resource.getUserInfo(), resource.getHost(), resource.getPort(), resource.getPath(), null, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        this.eventType = eventType;
    }

    /**
     * Returns the path to the subscribable resource.
     *
     * @return the path to the subscribable resource.
     */
    public @NotNull URI getResource() {
        return resource;
    }

    /**
     * Returns the class of the event type.
     *
     * @return the class of the event type.
     */
    public @NotNull Class<T> getEventType() {
        return eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscribableEvent<?> that = (SubscribableEvent<?>) o;
        return getResource().equals(that.getResource()) && getEventType().equals(that.getEventType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResource(), getEventType());
    }

    @Override
    public String toString() {
        return "SubscribableEvent{" +
                "resource=" + resource +
                ", eventType=" + eventType +
                '}';
    }
}
