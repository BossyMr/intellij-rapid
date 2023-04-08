package com.bossymr.network.client;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.ResponseConverter;
import com.bossymr.network.ResponseConverterFactory;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.client.proxy.EntityProxy;
import com.bossymr.network.client.proxy.ListProxy;
import com.bossymr.network.client.response.EntityConverter;
import com.bossymr.network.client.response.ResponseModelConverter;
import com.bossymr.network.client.response.StringConverter;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.entity.EntityInvocationHandler;
import com.bossymr.network.entity.ServiceInvocationHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * A {@code NetworkManager} is used to create entity instances connected to a remote server. A {@code NetworkManager} is
 * created by {@link #newBuilder(URI)} and connected to a remote server. A managed entity or service can be created
 * using {@link #createEntity(Class, EntityModel)} or {@link #createService(Class)}. Alternatively, an unmanaged entity
 * can be created using {@link #createLightEntity(Class, EntityModel)}.
 * <p>
 * An entity can be either managed or unmanaged. A managed entity can send requests to the remote server while an
 * unmanaged entity cannot.
 */
public class NetworkManager {

    private final @NotNull NetworkClient networkClient;

    private final @NotNull Map<Class<?>, Object> cache = new WeakHashMap<>();

    private final @NotNull Set<ResponseConverterFactory> converters;

    private volatile boolean closed;

    private NetworkManager(@NotNull Builder builder) {
        this.networkClient = new NetworkClient(builder.path, builder.credentials);
        this.converters = builder.converters;
    }

    /**
     * Creates a new unmanaged {@code Entity} of the specified type with the specified state. Any attempt to send a
     * request from this entity will fail.
     *
     * @param entityType the entity type.
     * @param model the entity state.
     * @param <T> the entity type.
     * @return the entity.
     * @see #createEntity(Class, EntityModel)
     */
    public static <T> @Nullable T createLightEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        return createEntity(null, entityType, model);
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T createEntity(@Nullable NetworkManager manager, @NotNull Class<T> entityType, @NotNull EntityModel model) {
        Map<String, Class<? extends T>> entities = getEntityType(entityType);
        String modelType = model.type().endsWith("-li") ? model.type().substring(0, model.type().length() - "-li".length()) : model.type();
        if (entities.containsKey(modelType)) {
            Class<? extends T> type = entities.get(modelType);
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type, EntityProxy.class}, new EntityInvocationHandler(manager, type, model));
        }
        return null;
    }

    private static <T> @NotNull Map<String, Class<? extends T>> getEntityType(Class<T> entityType) {
        Map<String, Class<? extends T>> entities = new HashMap<>();
        getEntityType(entities, entityType);
        return Collections.unmodifiableMap(entities);
    }

    @SuppressWarnings("unchecked")
    private static <T> void getEntityType(Map<String, Class<? extends T>> entities, Class<? extends T> entityType) {
        if (entityType.getAnnotation(Entity.class) == null) {
            throw new IllegalArgumentException("'" + entityType.getName() + "' is not annotated with Entity");
        }
        Entity entity = entityType.getAnnotation(Entity.class);
        for (String name : entity.value()) {
            if (entities.containsKey(name)) {
                throw new IllegalArgumentException("'" + entityType.getName() + "' (" + name + ") is declared more than once");
            }
            entities.put(name, entityType);
        }
        for (Class<?> subtype : entity.subtype()) {
            if (entityType.equals(subtype)) {
                throw new IllegalArgumentException("'" + subtype.getName() + "' cannot be declared as subtype of itself");
            } else if (entityType.isAssignableFrom(subtype)) {
                getEntityType(entities, (Class<? extends T>) subtype);
            } else {
                throw new IllegalArgumentException("'" + subtype.getName() + "' cannot be declared as subtype of '" + entityType.getName() + "' - type does not implement supertype");
            }
        }
    }

    /**
     * Creates a new {@code Builder}.
     *
     * @return a new {@code Builder}.
     */
    public static @NotNull Builder newBuilder(@NotNull URI path) {
        return new Builder(path);
    }

    /**
     * Returns the {@code NetworkClient} of this {@code NetworkManager}. All entities sent by entities managed by this
     * {@code EntityManager} will be sent to this {@code NetworkClient}.
     *
     * @return the {@code NetworkClient}.
     */
    public @NotNull NetworkClient getNetworkClient() {
        return networkClient;
    }

    /**
     * Creates a new {@code NetworkQuery} which, when executed, will send the specified request and convert it into the
     * specified type.
     *
     * @param type the response type.
     * @param request the request.
     * @param <T> the response type.
     * @return a {@code NetworkQuery}.
     */
    public <T> @NotNull NetworkQuery<T> createQuery(@NotNull Class<T> type, @NotNull HttpRequest request) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return createQuery(GenericType.of(type), request);
    }

    /**
     * Creates a new {@code NetworkQuery} which, when executed, will send the specified request and convert it into the
     * specified type.
     *
     * @param type the response type.
     * @param request the request.
     * @param <T> the response type.
     * @return a {@code NetworkQuery}.
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull NetworkQuery<T> createQuery(@NotNull GenericType<T> type, @NotNull HttpRequest request) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return () -> {
            if (type.getRawType().equals(List.class)) {
                ParameterizedType parameterizedType = (ParameterizedType) type.getType();
                Class<T> typeArgument = (Class<T>) parameterizedType.getActualTypeArguments()[0];
                return (T) new ListProxy<>(NetworkManager.this, typeArgument, request);
            }
            HttpResponse<byte[]> response = networkClient.send(request);
            for (ResponseConverterFactory converter : converters) {
                ResponseConverter<T> responseConverter = converter.create(NetworkManager.this, type);
                if (responseConverter != null) {
                    return responseConverter.convert(response);
                }
            }
            return null;
        };
    }

    /**
     * Creates a new {@code SubscribableNetworkQuery} which will subscribe to the specified event.
     *
     * @param subscribableEvent the event.
     * @param <T> the event type.
     * @return a {@code SubscribableNetworkQuery}.
     */
    public <T> @NotNull SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> subscribableEvent) {
        return (priority, listener) -> networkClient.subscribe(subscribableEvent, priority, (entity, event) -> {
            T response = createEntity(subscribableEvent.getEventType(), event);
            if (response != null) {
                listener.onEvent(entity, response);
            }
        });
    }

    /**
     * Creates a new managed {@code Entity} of the specified type and with the specified state.
     *
     * @param entityType the entity type.
     * @param model the entity state.
     * @param <T> the entity type.
     * @return the entity.
     * @see #createLightEntity(Class, EntityModel)
     */
    public <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return createEntity(this, entityType, model);
    }

    /**
     * Creates a new managed {@code Service} of the specified type.
     *
     * @param serviceType the service type.
     * @param <T> the service type.
     * @return the service.
     */
    @SuppressWarnings("unchecked")
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        if (cache.containsKey(serviceType)) {
            return (T) cache.get(serviceType);
        }
        T service = (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[]{serviceType, EntityProxy.class}, new ServiceInvocationHandler(this, serviceType));
        cache.put(serviceType, service);
        return service;
    }

    /**
     * Closes this {@code NetworkManager}. Any attempt to send a request, either with a {@code NetworkQuery} or with an
     * {@code Entity}, will fail.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the current thread is interrupted.
     */
    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        closed = true;
        networkClient.close();
    }

    public static class Builder {

        private final @NotNull Set<ResponseConverterFactory> converters = new HashSet<>();
        private @NotNull URI path;
        private @Nullable Credentials credentials;

        public Builder(@NotNull URI path) {
            this.path = path;
            this.converters.addAll(Set.of(ResponseModelConverter.FACTORY, StringConverter.FACTORY, EntityConverter.FACTORY));
        }

        public @NotNull Builder setPath(@NotNull URI path) {
            this.path = path;
            return this;
        }

        public @NotNull Builder setCredentials(@NotNull Credentials credentials) {
            this.credentials = credentials;
            return this;
        }

        public @NotNull Builder withConverter(@NotNull ResponseConverterFactory converter) {
            this.converters.add(converter);
            return this;
        }

        public @NotNull NetworkManager build() {
            return new NetworkManager(this);
        }
    }

}
