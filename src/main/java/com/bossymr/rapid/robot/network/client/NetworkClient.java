package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.query.AsynchronousQuery;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscriptionPriority;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * A {@code NetworkClient} capable of sending, receiving and subscribing to events on a remote robot.
 */
public interface NetworkClient {

    @SuppressWarnings("unchecked")
    static <T> @NotNull T newSimpleEntity(@NotNull Model model, @NotNull Class<T> entityType) {
        return (T) Proxy.newProxyInstance(
                entityType.getClassLoader(),
                new Class[]{entityType},
                new EntityInvocationHandler(entityType, null, model));
    }

    /**
     * Creates a new service of the specified type.
     *
     * @param serviceType the class of the service type.
     * @param <T> the service type.
     * @return a service of the specified type.
     * @throws IllegalArgumentException if the specified type is not annotated with {@link Service}.
     */
    <T> @NotNull T newService(@NotNull Class<T> serviceType);

    /**
     * Creates a new entity of the specified type and with the specified model.
     *
     * @param model the model of the entity.
     * @param entityType the class of the entity.
     * @param <T> the entity type.
     * @return an entity of the specified type.
     */
    <T> @NotNull T newEntity(@NotNull Model model, @NotNull Class<T> entityType);

    @NotNull URI getDefaultPath();

    /**
     * Sends the specified request synchronously and returns the response.
     * <p>
     * This method will also handle authentication and logging. If the request was not successful, this method will
     * throw a {@link ResponseStatusException}.
     *
     * @param httpRequest the request.
     * @return the response.
     * @throws IOException if an I/O error occurs.
     */
    @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest httpRequest) throws IOException, InterruptedException;

    /**
     * Sends the specified request asynchronously and returns a future with the response.
     * <p>
     * This method will also handle authentication and logging. If the request was not successful, the future will
     * complete exceptionally with a {@link ResponseStatusException}.
     *
     * @param httpRequest the request.
     * @return the asynchronous response.
     */
    @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest httpRequest);

    /**
     * Subscribes to the specified path synchronously , with the specified priority and event callback. The subscription
     * key is used to differentiate subscriptions, and is required to unsubscribe the same subscription. A key cannot be
     * used for multiple subscriptions.
     *
     * @param key the subscription key.
     * @param path the subscription path.
     * @param priority the subscription priority.
     * @param onEvent the subscription callback.
     * @param returnType the class of the return type.
     * @param <T> the return type.
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if the subscription key is already associated with an active subscription.
     */
    <T> void subscribe(@NotNull Object key, @NotNull String path, @NotNull SubscriptionPriority priority, @NotNull Consumer<T> onEvent, @NotNull Class<T> returnType) throws IOException, InterruptedException;

    /**
     * Unsubscribes from the subscription with the specified key.
     *
     * @param key the subscription key.
     * @throws IOException if an I/O error occurs.
     * @throws IllegalArgumentException if the subscription key is not associated with an active subscription.
     */
    void unsubscribe(@NotNull Object key) throws IOException, InterruptedException;

    /**
     * Creates a new query, which will send the specified request and convert it into the specified type. The return
     * type must either be an entity type (which implements {@link EntityModel} and is annotated with {@link Entity}) or
     * a {@link List} or {@link Set}, which contains an entity type.
     *
     * @param httpRequest the request.
     * @param returnType the class of the return type.
     * @param <T> the return type.
     * @return a query.
     */
    <T> @NotNull Query<T> newQuery(@NotNull HttpRequest httpRequest, @NotNull Type returnType);

    /**
     * Creates a new asynchronous query, which will send the specified request.
     *
     * @param httpRequest the request.
     * @return an asynchronous query.
     */
    @NotNull AsynchronousQuery newAsynchronousQuery(@NotNull HttpRequest httpRequest);

    /**
     * Creates a new subscribable query, which will subscribe to the specified resource and convert events into the
     * specified type. The event type must implement {@link EntityModel} and be annotated with {@link Entity}.
     *
     * @param path the path to subscribe to.
     * @param returnType the class of the event type.
     * @param <T> the event type.
     * @return a subscribable query.
     */
    <T> @NotNull SubscribableQuery<T> newSubscribableQuery(@NotNull String path, @NotNull Class<T> returnType);

    void close() throws IOException;
}
