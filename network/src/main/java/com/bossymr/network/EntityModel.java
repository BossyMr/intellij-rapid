package com.bossymr.network;

import com.bossymr.network.client.NetworkEngine;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;

/**
 * An {@code EntityModel} is an entity provided by a robot.
 */
public interface EntityModel {

    @NotNull NetworkEngine getNetworkEngine();

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
    @NotNull Map<String, URI> getLinks();

    /**
     * Returns the fields of this entity.
     *
     * @return the fields of this entity.
     */
    @NotNull Map<String, String> getFields();

    /**
     * Returns the link with the specified type.
     *
     * @param type the type.
     * @return the link with the specified type.
     */
    URI getLink(@NotNull String type);

    /**
     * Returns the field with the specified type.
     *
     * @param type the type.
     * @return the field with the specified type.
     */
    String getField(@NotNull String type);

}
