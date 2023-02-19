package com.bossymr.network;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.entity.EntityInvocationHandler;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * An {@code EntityModel} is an entity provided by a robot.
 */
public interface EntityModel {

    static <T extends EntityModel> @NotNull T move(@NotNull T entity, @NotNull NetworkEngine networkEngine) {
        @SuppressWarnings("unchecked")
        Class<T> entityType = (Class<T>) entity.getClass().getInterfaces()[0];
        Model model = EntityInvocationHandler.getInstance(entity).getModel();
        return Objects.requireNonNull(networkEngine.createEntity(entityType, model));
    }

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
