package com.bossymr.network.client;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.entity.EntityInvocationHandler;
import com.bossymr.network.entity.ServiceInvocationHandler;
import com.bossymr.network.model.CollectionModel;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class EntityFactory {

    private final @NotNull NetworkEngine engine;
    private final @NotNull NetworkClient client;

    public EntityFactory(@NotNull NetworkEngine engine, @NotNull NetworkClient client) {
        this.engine = engine;
        this.client = client;
    }

    public static <T> @NotNull Map<String, Class<? extends T>> getEntityType(Class<T> entityType) {
        Map<String, Class<? extends T>> entities = new HashMap<>();
        getEntityType(entities, entityType);
        return Collections.unmodifiableMap(entities);
    }

    @SuppressWarnings("unchecked")
    private static <T> void getEntityType(Map<String, Class<? extends T>> entities, Class<? extends T> entityType) {
        validateType(entityType);
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

    private static void validateType(Class<?> entityType) {
        if (entityType.getAnnotation(Entity.class) == null) {
            throw new IllegalArgumentException("'" + entityType.getName() + "' is not annotated with Entity");
        }
        if (!(EntityModel.class.isAssignableFrom(entityType))) {
            throw new IllegalArgumentException("'" + entityType.getName() + "' does not implement EntityModel");
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull T createService(@NotNull Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(),
                new Class[]{serviceType},
                new ServiceInvocationHandler(engine));
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T createEntity(@NotNull Class<T> entityType, @NotNull Model model) {
        Map<String, Class<? extends T>> arguments = EntityFactory.getEntityType(entityType);
        if (arguments.containsKey(model.getType())) {
            Class<? extends T> returnType = arguments.get(model.getType());
            return (T) Proxy.newProxyInstance(
                    returnType.getClassLoader(),
                    new Class[]{returnType},
                    new EntityInvocationHandler(engine, model));
        }
        return null;
    }

    public <T> @Nullable T convert(@NotNull HttpRequest request, @NotNull Type returnType) throws IOException, InterruptedException {
        List<Model> models = get(request);
        if (models == null) return null;
        return convert(models, returnType);
    }

    public <T> @NotNull CompletableFuture<T> convertAsync(@NotNull HttpRequest request, @NotNull Type returnType) {
        return getAsync(request, new ArrayList<>())
                .thenApplyAsync(models -> {
                    if (models == null) return null;
                    return convert(models, returnType);
                });
    }

    @SuppressWarnings("unchecked")
    private <T> @Nullable T convert(@NotNull List<Model> models, @NotNull Type returnType) {
        if (returnType == Void.class) return null;
        Class<? extends EntityModel> entityType = getReturnType(returnType);
        List<?> entities = models.stream()
                .map(model -> engine.createEntity(entityType, model))
                .filter(Objects::nonNull)
                .toList();
        if (returnType instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() instanceof Class<?> classType) {
                if (classType == List.class) {
                    return (T) entities;
                }
                if (classType == Set.class) {
                    return (T) Set.copyOf(entities);
                }
            }
        }
        if (returnType instanceof Class<?> classType) {
            if (entities.size() != 1) {
                throw new IllegalArgumentException("Could not convert '" + models + "' into a single entity of '" + classType.getName() + "'");
            }
            return (T) entities.get(0);
        }
        throw new IllegalArgumentException("Could not convert '" + models + "' into '" + returnType + "'");
    }

    @SuppressWarnings("unchecked")
    private @NotNull Class<? extends EntityModel> getReturnType(@NotNull Type returnType) {
        if (returnType instanceof Class<?> classType) {
            validateType(classType);
            return (Class<? extends EntityModel>) classType;
        }
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if (arguments.length == 1) {
                if (parameterizedType.getRawType() == List.class || parameterizedType.getRawType() == Set.class) {
                    Type argument = arguments[0];
                    if (argument instanceof Class<?> classType) {
                        validateType(classType);
                        return (Class<? extends EntityModel>) classType;
                    }
                }
            }
        }
        throw new IllegalArgumentException("'" + returnType + "' is not supported");
    }

    private @Nullable List<Model> get(@NotNull HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = client.send(request);
        byte[] body = response.body();
        if (body.length == 0) return null;
        CollectionModel collectionModel = CollectionModel.convert(body);
        List<Model> models = new ArrayList<>(collectionModel.getModels());
        onSingleEntity(models, collectionModel);
        while (collectionModel.getLink("next") != null) {
            HttpRequest next = HttpRequest.newBuilder(request, (n, v) -> true)
                    .uri(collectionModel.getLink("next"))
                    .build();
            response = client.send(next);
            collectionModel = CollectionModel.convert(response.body());
            models.addAll(collectionModel.getModels());
        }
        return models;
    }

    private @NotNull CompletableFuture<List<Model>> getAsync(@NotNull HttpRequest request, @NotNull List<Model> models) {
        return client.sendAsync(request)
                .thenComposeAsync(response -> {
                    byte[] body = response.body();
                    if (body.length == 0) return CompletableFuture.completedFuture(null);
                    CollectionModel collectionModel = CollectionModel.convert(body);
                    boolean isEmpty = models.isEmpty();
                    models.addAll(collectionModel.getModels());
                    if (isEmpty) {
                        onSingleEntity(models, collectionModel);
                    }
                    if (collectionModel.getLink("next") != null) {
                        HttpRequest next = HttpRequest.newBuilder(request, (n, v) -> true)
                                .uri(collectionModel.getLink("next"))
                                .build();
                        return getAsync(next, models);
                    }
                    return CompletableFuture.completedFuture(models);
                });
    }

    private void onSingleEntity(@NotNull List<Model> models, @NotNull CollectionModel collectionModel) {
        if (collectionModel.getLink("self") != null) {
            for (Model model : models) {
                if (model.getType().endsWith("-li")) continue;
                model.getLinks().putIfAbsent("self", collectionModel.getLink("self"));
            }
        }
    }

}
