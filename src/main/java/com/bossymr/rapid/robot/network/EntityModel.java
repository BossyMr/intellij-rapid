package com.bossymr.rapid.robot.network;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * An {@code EntityModel} is an entity provided by a robot.
 */
public interface EntityModel {

    /**
     * Returns the title of this entity.
     *
     * @return the title of this entity.
     */
    @NotNull String getTitle();

    /**
     * Returns the type of this entity.
     *
     * @return the type of this entity.
     */
    @NotNull String getType();

    /**
     * Returns the links of this entity.
     *
     * @return the links of this entity.
     */
    @NotNull List<Link> getLinks();

}
