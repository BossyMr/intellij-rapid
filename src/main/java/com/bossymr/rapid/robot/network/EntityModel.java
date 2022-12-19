package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.client.NetworkClient;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;

/**
 * An {@code EntityModel} is an entity provided by a robot.
 */
public interface EntityModel {

    @NotNull NetworkClient getNetworkClient();

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

    default URI getLink(@NotNull String relationship) {
        return getLinks().get(relationship);
    }

    default String getField(@NotNull String type) {
        return getFields().get(type);
    }

}
