package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code EntityProxy} represents a network entity wrapping an {@link EntityModel}
 * <p>
 * An entity proxy will lazy load itself. This means that if the entity was created with a model representing a minified
 * state, for example a {@code -li} entity, the entity will only attempt to fetch the full entity when a property is
 * retrieved that doesn't exist in the minified state. This allows one to retrieve a list of entities without fetching
 * the full state of each entity individually - all at once.
 */
public interface EntityProxy extends NetworkProxy {

    /**
     * Refreshes the state of this entity.
     *
     * @throws ProxyException if an I/O error occurs, or if this entity cannot be refreshed.
     */
    void refresh();

    /**
     * {@return the state representing this entity}
     */
    @NotNull EntityModel getModel();
}
