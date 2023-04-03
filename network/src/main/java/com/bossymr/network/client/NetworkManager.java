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

public class NetworkManager {

    private final @NotNull NetworkClient networkClient;

    private final @NotNull Map<Class<?>, Object> cache = new WeakHashMap<>();

    private final @NotNull List<ResponseConverterFactory> converters;

    private volatile boolean closed;

    public NetworkManager(@NotNull URI defaultPath, @NotNull Credentials credentials) {
        this.networkClient = new NetworkClient(defaultPath, credentials);
        this.converters = List.of(ResponseModelConverter.FACTORY, StringConverter.FACTORY, EntityConverter.FACTORY);
    }

    public static <T> @Nullable T createLightEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        return createEntity(null, entityType, model);
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T createEntity(@Nullable NetworkManager manager, @NotNull Class<T> entityType, @NotNull EntityModel model) {
        Map<String, Class<? extends T>> entities = getEntityType(entityType);
        if (entities.containsKey(model.type())) {
            Class<? extends T> type = entities.get(model.type());
            return (T) Proxy.newProxyInstance(entityType.getClassLoader(),
                    new Class[]{entityType, EntityProxy.class},
                    new EntityInvocationHandler(manager, type, model));
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

    public @NotNull NetworkClient getNetworkClient() {
        return networkClient;
    }

    public <T> @NotNull NetworkQuery<T> createQuery(@NotNull Class<T> type, @NotNull HttpRequest request) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return createQuery(GenericType.of(type), request);
    }

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
                ResponseConverter<T> responseConverter = converter.create(NetworkManager.this, response, type);
                if (responseConverter != null) {
                    return responseConverter.convert(response);
                }
            }
            return null;
        };
    }

    public <T> @NotNull SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> subscribableEvent) {
        return (priority, listener) -> networkClient.subscribe(subscribableEvent, priority, (entity, event) -> {
            T response = createEntity(subscribableEvent.getEventType(), event);
            if (response != null) {
                listener.onEvent(entity, response);
            }
        });
    }

    public <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        return createEntity(this, entityType, model);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        if (closed) {
            throw new IllegalStateException("NetworkManager is closed");
        }
        if (cache.containsKey(serviceType)) {
            return (T) cache.get(serviceType);
        }
        T service = (T) Proxy.newProxyInstance(serviceType.getClassLoader(),
                new Class[]{serviceType, EntityProxy.class},
                new ServiceInvocationHandler(this, serviceType));
        cache.put(serviceType, service);
        return service;
    }

    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        closed = true;
        networkClient.close();
    }

}
