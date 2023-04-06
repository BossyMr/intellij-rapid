package com.bossymr.network.client.proxy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

/**
 * A {@code EntityProxy} is an entity proxy with state.
 */
public interface EntityProxy extends NetworkProxy {

    /**
     * Refreshes the state of this entity.
     */
    void refresh();

    @NotNull String getType();

    @NotNull String getTitle();

    @Nullable URI getReference(@NotNull String type);

    @Nullable String getProperty(@NotNull String type);
}
