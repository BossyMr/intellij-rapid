package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.entity.EntityInvocationHandler;
import com.bossymr.network.entity.ServiceInvocationHandler;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A {@code NetworkEngine} is used to create services and entities, as-well as instances of {@link NetworkCall} and
 * {@link SubscribableNetworkCall}.
 * <p>
 * A {@code NetworkEngine} can be closed using {@link #close()}. This will close any {@link NetworkCall} and
 * {@link SubscribableNetworkCall} created with this {@code NetworkEngine}, which will prohibit any new requests being
 * called. Additionally, any attempt to create a new {@code NetworkCall} or {@code SubscribableNetworkCall} will fail.
 * <p>
 * A {@link DelegatingNetworkEngine} can be created to subclass this {@code NetworkEngine} with a specific policy for
 * handling successful and unsuccessful responses.
 */
public class NetworkEngine implements AutoCloseable {

    private final @NotNull Set<NetworkCall<?>> requests = ConcurrentHashMap.newKeySet();
    private final @NotNull Set<SubscribableNetworkCall<?>> subscriptions = ConcurrentHashMap.newKeySet();

    private final @NotNull NetworkClient client;
    private final @NotNull EntityFactory entityFactory;
    private final @NotNull RequestFactory requestFactory;

    private volatile boolean closed;

    protected NetworkEngine(@NotNull NetworkClient client, @NotNull EntityFactory entityFactory, @NotNull RequestFactory requestFactory) {
        this.client = client;
        this.entityFactory = entityFactory;
        this.requestFactory = requestFactory;
    }

    public NetworkEngine(@NotNull URI defaultPath, @NotNull Supplier<Credentials> credentials) {
        this.client = new HttpNetworkClient(defaultPath, credentials);
        this.entityFactory = new EntityFactory(this, client);
        this.requestFactory = new RequestFactory(this);
    }

    public NetworkEngine(@NotNull String defaultPath, @NotNull Supplier<Credentials> credentials) {
        this(URI.create(defaultPath), credentials);
    }

    public @NotNull NetworkClient getNetworkClient() {
        return client;
    }

    protected @NotNull NetworkEngine getNetworkEngine() {
        return this;
    }

    protected @NotNull EntityFactory getEntityFactory() {
        return entityFactory;
    }

    public @NotNull RequestFactory getRequestFactory() {
        return requestFactory;
    }

    public @NotNull RequestBuilder createRequest() {
        return getNetworkClient().createRequest();
    }

    /**
     * Creates a new service of the specified type.
     *
     * @param serviceType the service type.
     * @param <T> the service type.
     * @return a new service.
     * @throws IllegalArgumentException if the type is not annotated with {@link Service}.
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[]{serviceType}, new ServiceInvocationHandler(getNetworkEngine()));
    }

    @SuppressWarnings("unchecked")
    public static <T> @Nullable T createEntity(@Nullable NetworkEngine networkEngine, @NotNull Class<T> entityType, @NotNull Model model) {
        Map<String, Class<? extends T>> arguments = EntityFactory.getEntityType(entityType);
        if (arguments.containsKey(model.getType())) {
            Class<? extends T> returnType = arguments.get(model.getType());
            return (T) Proxy.newProxyInstance(returnType.getClassLoader(), new Class[]{returnType}, new EntityInvocationHandler(networkEngine, model));
        }
        return null;
    }

    /**
     * Creates a new entity of the specified type and the specified underlying intermediate model.
     *
     * @param entityType the entity type.
     * @param model the underlying response.
     * @param <T> the entity type.
     * @return a new entity, or {@code null} if the specified model could not be converted to the specified type.
     * @throws IllegalArgumentException if the type is not annotated with {@link Entity}.
     */
    public <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull Model model) {
        return createEntity(getNetworkEngine(), entityType, model);
    }

    /**
     * Creates a new {@code NetworkCall} which will call the specified request and convert the response into the
     * specified type.
     *
     * @param request the request.
     * @param returnType the response type.
     * @param <T> the response type.
     * @return a new {@code NetworkCall}.
     */
    public <T> @NotNull NetworkCall<T> createNetworkCall(@NotNull HttpRequest request, @NotNull Type returnType) {
        return createNetworkCall(getNetworkEngine(), request, returnType);
    }

    protected <T> @NotNull NetworkCall<T> createNetworkCall(@NotNull NetworkEngine engine, @NotNull HttpRequest request, @NotNull Type returnType) {
        if (closed) {
            throw new IllegalArgumentException();
        }
        HttpNetworkCall<T> networkCall = new HttpNetworkCall<>(engine.getEntityFactory(), request, returnType);
        requests.add(networkCall);
        return networkCall;
    }

    /**
     * Creates a new {@code SubscribableNetworkCall} which will subscribe to the specified event.
     *
     * @param event the event.
     * @param <T> the event type.
     * @return a new {@code SubscribableNetworkCall}.
     */
    public <T> @NotNull SubscribableNetworkCall<T> createSubscribableNetworkCall(@NotNull SubscribableEvent<T> event) {
        return createSubscribableNetworkCall(getNetworkEngine(), event);
    }

    protected <T> @NotNull SubscribableNetworkCall<T> createSubscribableNetworkCall(@NotNull NetworkEngine engine, @NotNull SubscribableEvent<T> event) {
        if (closed) throw new IllegalArgumentException();
        HttpSubscribableNetworkCall<T> networkCall = new HttpSubscribableNetworkCall<>(engine, event);
        subscriptions.add(networkCall);
        return networkCall;
    }

    /**
     * Closes this {@code NetworkEngine} and all created {@code NetworkCall} and {@code SubscribableNetworkCall}
     * instances. All requests created by this {@code NetworkCall} are canceled and all ongoing subscriptions are
     * disconnected.
     *
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if this {@code NetworkEngine} is interrupted.
     */
    @Override
    public void close() throws IOException, InterruptedException {
        if (closed) return;
        closed = true;
        requests.forEach(NetworkCall::close);
        for (SubscribableNetworkCall<?> subscription : subscriptions) {
            subscription.close();
        }
    }
}
