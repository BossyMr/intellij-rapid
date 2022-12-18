package com.bossymr.rapid.robot.network.client.impl;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.client.model.ModelUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * A collection of utility methods used to convert a {@link CollectionModel} into an entity.
 */
public final class EntityUtil {

    private EntityUtil() {
        throw new AssertionError();
    }

    public static <T> @Nullable T convert(@NotNull NetworkClient networkClient, @NotNull HttpResponse<byte[]> response, @NotNull Type returnType) throws IOException, InterruptedException {
        byte[] body = response.body();
        if (body.length == 0) return null;
        CollectionModel collectionModel = ModelUtil.convert(body);
        List<Model> models = new ArrayList<>(collectionModel.getModels());
        handleSingleEntity(collectionModel, models);
        while (collectionModel.getLink("next") != null) {
            HttpRequest request = HttpRequest.newBuilder(response.request(), (n, v) -> true)
                    .uri(collectionModel.getLink("next"))
                    .build();
            HttpResponse<byte[]> next = networkClient.send(request);
            collectionModel = ModelUtil.convert(next.body());
            models.addAll(collectionModel.getModels());
        }
        return convert(networkClient, models, returnType);
    }

    public static <T> @NotNull CompletableFuture<T> convertAsync(@NotNull NetworkClient networkClient, @NotNull HttpResponse<byte[]> response, @NotNull Type returnType) {
        return convertAsync(networkClient, new ArrayList<>(), response, returnType);
    }

    private static <T> @NotNull CompletableFuture<T> convertAsync(@NotNull NetworkClient networkClient, @NotNull List<Model> models, @NotNull HttpResponse<byte[]> response, @NotNull Type returnType) {
        byte[] body = response.body();
        if (body.length == 0) return CompletableFuture.completedFuture(null);
        CollectionModel collectionModel;
        try {
            collectionModel = ModelUtil.convert(body);
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
        boolean isFirst = models.isEmpty();
        models.addAll(collectionModel.getModels());
        if (isFirst) {
            handleSingleEntity(collectionModel, models);
        }
        if (collectionModel.getLink("next") != null) {
            HttpRequest request = HttpRequest.newBuilder(response.request(), (n, v) -> true)
                    .uri(collectionModel.getLink("next"))
                    .build();
            return networkClient.sendAsync(request)
                    .thenComposeAsync(next -> convertAsync(networkClient, models, next, returnType));
        } else {
            T entity = convert(networkClient, models, returnType);
            return CompletableFuture.completedFuture(entity);
        }
    }

    private static void handleSingleEntity(@NotNull CollectionModel collectionModel, @NotNull List<Model> models) {
        if (models.size() == 1 && collectionModel.getModels().size() == 1) {
            Model model = models.get(0);
            if (model.getLink("self") == null) {
                model.getLinks().putAll(collectionModel.getLinks());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> @Nullable T convert(@NotNull NetworkClient networkClient, @NotNull List<Model> models, @NotNull Type returnType) {
        if (Void.class.equals(returnType)) return null;
        Class<?> entityType = getReturnType(returnType);
        Map<String, Class<?>> entityTypes = getReturnTypes(entityType);
        models = models.stream()
                .filter(model -> entityTypes.containsKey(model.getType()))
                .toList();
        List<?> entities = models.stream()
                .map(model -> networkClient.newEntity(model, entityTypes.get(model.getType())))
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

    public static @NotNull Class<?> getReturnType(@NotNull Type returnType) {
        if (returnType instanceof Class<?> classType) {
            validateType(classType);
            return classType;
        }
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if (arguments.length == 1) {
                if (parameterizedType.getRawType() == List.class || parameterizedType.getRawType() == Set.class) {
                    Type argument = arguments[0];
                    if (argument instanceof Class<?> classType) {
                        validateType(classType);
                        return classType;
                    }
                }
            }
        }
        throw new IllegalArgumentException("'" + returnType + "' is not supported");
    }

    public static @NotNull Map<String, Class<?>> getReturnTypes(Class<?> returnType) {
        Map<String, Class<?>> entities = new HashMap<>();
        getReturnTypes(entities, returnType);
        return Collections.unmodifiableMap(entities);
    }

    private static void getReturnTypes(Map<String, Class<?>> entities, Class<?> returnType) {
        validateType(returnType);
        Entity entity = returnType.getAnnotation(Entity.class);
        for (String name : entity.value()) {
            if (entities.containsKey(name)) {
                throw new IllegalArgumentException("'" + returnType.getName() + "' (" + name + ") is declared more than once");
            }
            entities.put(name, returnType);
        }
        for (Class<?> subtype : entity.subtype()) {
            if (returnType.equals(subtype)) {
                throw new IllegalArgumentException("'" + subtype.getName() + "' cannot be declared as subtype of itself");
            } else if (returnType.isAssignableFrom(subtype)) {
                getReturnTypes(entities, subtype);
            } else {
                throw new IllegalArgumentException("'" + subtype.getName() + "' cannot be declared as subtype of '" + returnType.getName() + "' - type does not implement supertype");
            }
        }
    }

    private static void validateType(Class<?> classType) {
        if (classType.getAnnotation(Entity.class) == null) {
            throw new IllegalArgumentException("'" + classType.getName() + "' is not annotated with Entity");
        }
        if (!(EntityModel.class.isAssignableFrom(classType))) {
            throw new IllegalArgumentException("'" + classType.getName() + "' does not implement EntityModel");
        }
    }


}
