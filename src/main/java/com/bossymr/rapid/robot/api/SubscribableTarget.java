package com.bossymr.rapid.robot.api;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * A {@code SubscribableTarget} represents a subscribable endpoint.
 *
 * @param <T> the event type of the target.
 */
public class SubscribableTarget<T> {

    /**
     * The relative path to the target. This path will be resolved against the base path of the server.
     */
    private final @NotNull URI path;

    /**
     * The type of events sent through this target. The type must be a {@code Class}, since every entity is converted to
     * this type. This is unlike a regular network request, which can return multiple entities.
     */
    private final @NotNull Class<T> eventType;

    /**
     * Creates a new {@code SubscribableTarget} referencing a subscribable resource at the provided path.
     *
     * @param path the path to the subscribable resource.
     * @param eventType the class of the event type.
     */
    public SubscribableTarget(@NotNull URI path, @NotNull Class<T> eventType) {
        try {
            this.path = new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), null, null);
        } catch (URISyntaxException e) {
            // This should never throw an exception since we are only removing parts of the path: we aren't creating a
            // new path.
            throw new IllegalStateException("unreachable statement", e);
        }
        this.eventType = eventType;
    }

    /**
     * {@return the path to the subscribable resource}
     */
    public @NotNull URI getPath() {
        return path;
    }

    /**
     * {@return the event type}
     */
    public @NotNull Class<T> getType() {
        return eventType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscribableTarget<?> that = (SubscribableTarget<?>) o;
        return getPath().equals(that.getPath()) && getType().equals(that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getType());
    }

    @Override
    public String toString() {
        return "SubscribableTarget{" +
               "resource=" + path +
               ", eventType=" + eventType +
               '}';
    }
}
