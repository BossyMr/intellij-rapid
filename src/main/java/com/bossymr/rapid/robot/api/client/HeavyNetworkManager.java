package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.proxy.EntityProxy;
import com.bossymr.rapid.robot.api.client.proxy.NetworkProxy;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.api.entity.EntityInvocationHandler;
import com.bossymr.rapid.robot.api.entity.ServiceInvocationHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class HeavyNetworkManager implements NetworkManager {

    private final NetworkClient networkClient;
    private final @NotNull Set<NetworkManager.Listener> listeners = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public HeavyNetworkManager(@NotNull URI defaultPath, @Nullable Credentials credentials) {
        this.networkClient = new NetworkClient(defaultPath, credentials);
    }

    public static <T> @NotNull T createLightEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        return createEntity(null, entityType, model);
    }

    public static <T> @NotNull NetworkQuery<T> createQuery(@NotNull NetworkManager manager, @NotNull NetworkTarget<T> target) {
        return () -> {
            NetworkType<T> type = target.getType();
            HttpResponse<byte[]> response = manager.getNetworkClient().send(target);
            return type.convert(manager, response);
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull T createEntity(@Nullable NetworkManager manager, @NotNull Class<T> entityType, @NotNull EntityModel model) {
        Class<? extends T> actualType = getEntityType(entityType, model);
        if (actualType != null) {
            return (T) Proxy.newProxyInstance(
                    actualType.getClassLoader(),
                    new Class[]{actualType, EntityProxy.class},
                    new EntityInvocationHandler(manager, actualType, model));
        }
        throw new IllegalArgumentException(model.getType() + " could not be converted into " + entityType.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull T createService(@NotNull NetworkManager manager, @NotNull Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(),
                new Class[]{serviceType, NetworkProxy.class},
                new ServiceInvocationHandler(manager, serviceType));
    }

    private static <T> @Nullable Class<? extends T> getEntityType(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        Map<String, Class<? extends T>> entities = getEntityGraph(entityType);
        String type = model.getType();
        if (type.endsWith("-li")) {
            return entities.get(type.substring(0, type.length() - "-li".length()));
        } else {
            return entities.get(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> @NotNull Map<String, Class<? extends T>> getEntityGraph(@NotNull Class<? extends T> entityType) {
        Map<String, Class<? extends T>> entities = new HashMap<>();
        Entity entity = entityType.getAnnotation(Entity.class);
        if (entity == null) {
            throw new IllegalArgumentException(entityType.getName() + " is not annotated with Entity");
        }
        for (String name : entity.value()) {
            if (entities.containsKey(name)) {
                throw new IllegalArgumentException(entityType.getName() + " (" + name + ") is declared more than once");
            }
            entities.put(name, entityType);
        }
        for (Class<?> subtype : entity.subtype()) {
            if (entityType.equals(subtype)) {
                throw new IllegalArgumentException(subtype.getName() + " cannot be declared as subtype of itself");
            }
            if (!entityType.isAssignableFrom(subtype)) {
                throw new IllegalArgumentException(subtype.getName() + " does not implement supertype " + entityType.getName());
            }
            Map<String, Class<? extends T>> graph = getEntityGraph((Class<? extends T>) subtype);
            entities.putAll(graph);
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull T move(@NotNull T entity, @NotNull NetworkManager manager) {
        if (!(entity instanceof NetworkProxy proxy)) {
            throw new IllegalArgumentException("Argument '" + entity + "' doesn't represent an @Entity or @Service");
        }
        return (T) proxy.move(manager);
    }

    @Override
    public @NotNull NetworkClient getNetworkClient() {
        return networkClient;
    }

    @Override
    public <T> @NotNull T move(@NotNull T entity) {
        return HeavyNetworkManager.move(entity, this);
    }

    @Override
    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull NetworkTarget<T> request) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return createQuery(this, request);
    }

    @Override
    public @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableTarget<T> event) {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return (priority, listener) -> getNetworkClient().subscribe(event, priority, new SubscriptionListener<>() {
            @Override
            public void onEvent(@NotNull SubscriptionEntity entity, @NotNull EntityModel response) {
                T result = createEntity(event.getType(), response);
                listener.onEvent(entity, result);
            }

            @Override
            public void onClose(@NotNull SubscriptionEntity entity) {
                listener.onClose(entity);
            }
        });
    }

    @Override
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) throws IllegalArgumentException {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return createService(this, serviceType);
    }

    @Override
    public <T> @NotNull T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) throws IllegalArgumentException {
        if (closed) {
            throw new IllegalArgumentException("NetworkManager is closed");
        }
        return createEntity(this, entityType, model);
    }

    @Override
    public void subscribe(@NotNull Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (closed) {
            return;
        }
        for (NetworkManager.Listener listener : listeners) {
            listener.onClose();
        }
        closed = true;
        networkClient.close();
    }
}
