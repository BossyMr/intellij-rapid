package com.bossymr.network;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.client.*;
import com.bossymr.network.client.proxy.ProxyException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;

/**
 * A {@code NetworkManager} is connected to a remote server, and can create and manage entities and services.
 * <p>
 * An entity or service is an instance of an interface. A {@link Service service} has no state and can only contain
 * {@link Fetch requests}. An {@link Entity entity}, however, represents a resource, and can contain either requests or
 * methods which return a specific {@link Property property} of the entity.
 * <p>
 * An entity can be either managed or unmanaged. A service must always be managed. A managed entity will send all
 * requests to the {@code NetworkManager} managing it. However, an unmanaged entity will throw a {@link ProxyException}
 * if a request is invoked.
 */
public interface NetworkManager extends AutoCloseable {

    /**
     * Creates a new unmanaged entity.
     *
     * @param entityType the entity type.
     * @param model the entity state.
     * @param <T> the entity type.
     * @return the entity.
     * @throws IllegalArgumentException if the entity model could not be deserialized into the specified entity type, or
     * if the specified type is annotated as an {@link Entity entity}.
     */
    static <T> @NotNull T createLightEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        return HeavyNetworkManager.createLightEntity(entityType, model);
    }

    /**
     * Returns the {@code NetworkClient} used by this {@code NetworkManager} to handle network communication.
     *
     * @return the {@code NetworkClient} used by this {@code NetworkManager}.
     */
    @NotNull NetworkClient getNetworkClient();

    /**
     * Returns the {@code ResponseConverterFactory} instances used by this {@code NetworkManager}.
     *
     * @return the {@code ResponseConverterFactory} instances used by this {@code NetworkManager}.
     */
    @NotNull Set<ResponseConverterFactory> getConverters();

    /**
     * Creates a new {@code NetworkManager} which will delegate all requests to this {@code NetworkManager}. The created
     * {@code NetworkManager} can be closed independently of this {@code NetworkManager}, in which case only entities
     * managed by the child {@code NetworkManager} are closed. If this {@code NetworkManager} is closed, all child
     * {@code NetworkManager} are automatically closed.
     *
     * @return a new child {@code NetworkManager.}
     */
    @NotNull NetworkManager createLight();

    /**
     * Creates a new {@code NetworkQuery} which will send the specified request and convert the response into the
     * specified type.
     *
     * @param responseType the response type.
     * @param request the request.
     * @param <T> the response type.
     * @return the query.
     */
    default <T> @NotNull NetworkQuery<T> createQuery(@NotNull Class<T> responseType, @NotNull NetworkRequest request) {
        return createQuery(GenericType.of(responseType), request);
    }

    /**
     * Creates a new {@code NetworkQuery} which will send the specified request and convert the response into the
     * specified generic type.
     *
     * @param responseType the response type.
     * @param request the request.
     * @param <T> the response type.
     * @return the query.
     */
    <T> @NotNull NetworkQuery<T> createQuery(@NotNull GenericType<T> responseType, @NotNull NetworkRequest request);

    /**
     * Creates a new {@code SubscribableNetworkQuery} which will subscribe to the specified event.
     *
     * @param event the event.
     * @param <T> the event type.
     * @return the query.
     */
    @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> event);

    /**
     * Creates a new service managed by this {@code NetworkManager}.
     *
     * @param serviceType the service type.
     * @param <T> the service type.
     * @return the service.
     * @throws IllegalArgumentException if the specified type is not managed as a {@link Service service}.
     */
    <T> @NotNull T createService(@NotNull Class<T> serviceType) throws IllegalArgumentException;

    /**
     * Creates a new entity managed by this {@code NetworkManager}.
     *
     * @param entityType the entity type.
     * @param model the entity state.
     * @param <T> the entity type.
     * @return the entity.
     * @throws IllegalArgumentException if the entity model could not be deserialized into the specified entity type, or
     * if the specified type is annotated as an {@link Entity entity}.
     */
    <T> @NotNull T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException;

    /**
     * Close this {@code NetworkManager} and any ongoing subscriptions.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    @Override
    void close() throws IOException, InterruptedException;
}
