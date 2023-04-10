package com.bossymr.network.client;

import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.client.proxy.EntityProxy;
import com.bossymr.network.entity.EntityInvocationHandler;
import com.bossymr.network.entity.ServiceInvocationHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.http.HttpRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class NetworkAction implements AutoCloseable {

    private static final @NotNull Logger logger = LoggerFactory.getLogger(NetworkAction.class);

    private static final @NotNull ServiceCache cache = new ServiceCache();

    private final @NotNull Set<SubscriptionEntity> entities = ConcurrentHashMap.newKeySet();
    private final @NotNull NetworkManager manager;

    NetworkAction(@NotNull NetworkManager manager) {
        this.manager = manager;
    }

    public static <T> @Nullable T createLightEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        return createEntity(null, entityType, model);
    }

    public @NotNull NetworkManager getManager() {
        return manager;
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T createEntity(@Nullable NetworkAction action, @NotNull Class<T> entityType, @NotNull EntityModel model) {
        Map<String, Class<? extends T>> entities = getEntityType(entityType);
        String modelType = model.type().endsWith("-li") ? model.type().substring(0, model.type().length() - "-li".length()) : model.type();
        if (entities.containsKey(modelType)) {
            Class<? extends T> type = entities.get(modelType);
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type, EntityProxy.class}, new EntityInvocationHandler(action, type, model));
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

    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull Class<T> type, @NotNull HttpRequest request) {
        return createQuery(GenericType.of(type), request);
    }

    public @NotNull <T> NetworkQuery<T> createQuery(@NotNull GenericType<T> type, @NotNull HttpRequest request) {
        return manager.createQuery(this, type, request);
    }

    public @NotNull <T> SubscribableNetworkQuery<T> createSubscribableQuery(@NotNull SubscribableEvent<T> subscribableEvent) {
        logger.atDebug().log("Creating delegate SubscribableNetworkQuery '{}'", subscribableEvent);
        SubscribableNetworkQuery<T> delegateQuery = manager.createSubscribableQuery(this, subscribableEvent);
        return (priority, listener) -> {
            logger.atDebug().log("Subscribing to delegate SubscribableNetworkQuery '{}'", subscribableEvent);
            SubscriptionEntity delegate = delegateQuery.subscribe(priority, listener);
            AtomicReference<SubscriptionEntity> entity = new AtomicReference<>();
            entity.set(new SubscriptionEntity(delegate.getEvent(), delegate.getPriority()) {
                @Override
                public void unsubscribe() throws IOException, InterruptedException {
                    logger.atDebug().log("Unsubscribing from delegate SubscribableNetworkQuery '{}'", subscribableEvent);
                    entities.remove(entity.get());
                    delegate.unsubscribe();
                }

                @Override
                public void event(@NotNull EntityModel model) {
                    delegate.event(model);
                }
            });
            entities.add(entity.get());
            return delegate;
        };
    }

    public <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        return createEntity(this, entityType, model);
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        T cacheValue = cache.get(this, serviceType);
        if (cacheValue != null) {
            return cacheValue;
        }
        T service = (T) Proxy.newProxyInstance(serviceType.getClassLoader(), new Class[]{serviceType, EntityProxy.class}, new ServiceInvocationHandler(this, serviceType));
        cache.put(this, serviceType, service);
        return service;
    }

    @Override
    public void close() throws IOException, InterruptedException {
        for (SubscriptionEntity entity : entities) {
            logger.atDebug().log("Closing delegate subscription '{}'", entity);
            entity.unsubscribe();
        }
    }

    public static class ServiceCache {

        private final @NotNull Map<NetworkAction, Map<Class<?>, Object>> cache = new WeakHashMap<>();

        @SuppressWarnings("unchecked")
        public <T> @Nullable T get(@NotNull NetworkAction manager, @NotNull Class<T> type) {
            if (cache.containsKey(manager)) {
                Map<Class<?>, Object> map = cache.get(manager);
                if (map.containsKey(type)) {
                    return (T) map.get(type);
                }
            }
            return null;
        }

        public <T> void put(@NotNull NetworkAction manager, @NotNull Class<T> type, @NotNull T value) {
            cache.computeIfAbsent(manager, (k) -> new WeakHashMap<>());
            Map<Class<?>, Object> map = cache.get(manager);
            map.put(type, value);
        }

    }
}
