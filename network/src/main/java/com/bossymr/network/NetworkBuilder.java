package com.bossymr.network;

import com.bossymr.network.client.NetworkFactory;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code NetworkBuilder} is used to create a service instance.
 *
 * @param <T> the type of service.
 */
public interface NetworkBuilder<T> {

    /**
     * Sets the {@code NetworkFactory} to use to send and receive requests and responses.
     *
     * @param networkFactory the network client to use.
     * @return this builder.
     */
    @NotNull NetworkBuilder<T> setClient(@NotNull NetworkFactory networkFactory);

    /**
     * Creates a new service instance of this type.
     *
     * @return a new service instance of this type.
     */
    @NotNull T build();

}
