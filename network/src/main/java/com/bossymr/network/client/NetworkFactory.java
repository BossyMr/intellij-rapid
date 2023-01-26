package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.client.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;

/**
 * A {@code NetworkFactory} is used to create instances of {@link NetworkCall} and {@link SubscribableNetworkCall}.
 */
public interface NetworkFactory {

    /**
     * Creates a new {@code Service} of the specified type.
     *
     * @param serviceType the class of the service.
     * @param <T> the type of service.
     * @return a new {@code Service}.
     */
    <T> @NotNull T createService(@NotNull Class<T> serviceType);

    /**
     * Creates a new {@code Entity} of the specified type.
     *
     * @param entityType the class of the entity.
     * @param model the model of the entity.
     * @param <T> the type of entity.
     * @return a new {@code Entity}.
     */
    <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull Model model);

    /**
     * Creates a new builder for a {@link HttpRequest}.
     *
     * @return a new builder for a {@link HttpRequest}.
     */
    @NotNull RequestBuilder createRequest();

    /**
     * Creates a new {@code NetworkCall} which will send the specified request, and convert it into the specified type.
     *
     * @param request the request.
     * @param <T> the type of response body.
     * @return the network call.
     */
    <T> @NotNull NetworkCall<T> createNetworkCall(@NotNull HttpRequest request, @NotNull Type returnType);

    /**
     * Creates a new {@code SubscribableNetworkCall} which will subscribe to the specified event.
     *
     * @param event the event.
     * @param <T> the type of event body.
     * @return the subscribable network call.
     */
    <T> @NotNull SubscribableNetworkCall<T> createSubscribableNetworkCall(@NotNull SubscribableEvent<T> event);

}
