package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.client.entity.EntityModel;
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

    @NotNull EntityModel getModel();

    default @NotNull String getType() {
        return getModel().getType();
    }

    default @NotNull String getTitle() {
        return getModel().getTitle();
    }

    @Nullable URI getReference(@NotNull String type);

    @Nullable String getProperty(@NotNull String type);
}
