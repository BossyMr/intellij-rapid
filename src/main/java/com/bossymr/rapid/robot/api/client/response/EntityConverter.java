package com.bossymr.rapid.robot.api.client.response;

import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EntityConverter<T> implements ResponseConverter<T> {

    public static final ResponseConverterFactory FACTORY = new ResponseConverterFactory() {
        @Override
        public <E> ResponseConverter<E> create(@NotNull NetworkManager manager, @NotNull GenericType<E> type) {
            if (type.getRawType().isAnnotationPresent(Entity.class)) {
                return new EntityConverter<>(manager, type);
            }
            return null;
        }
    };

    private final @NotNull NetworkManager manager;
    private final @NotNull GenericType<T> type;

    public EntityConverter(@NotNull NetworkManager manager, @NotNull GenericType<T> type) {
        this.manager = manager;
        this.type = type;
    }

    private static void validateType(@NotNull Class<?> entityType) {
        if (entityType.getAnnotation(Entity.class) == null) {
            throw new ProxyException("'" + entityType.getName() + "' is not annotated with Entity");
        }
    }

    @Override
    public @Nullable T convert(@NotNull HttpResponse<byte[]> response) throws IOException, InterruptedException {
        return convert(response, type.getType());
    }

    public @Nullable T convert(@NotNull EntityModel model) {
        return convert(List.of(model), type.getType());
    }

    private @Nullable T convert(@NotNull HttpResponse<byte[]> response, @NotNull Type returnType) throws IOException, InterruptedException {
        List<EntityModel> models = get(response);
        if (models == null) return null;
        return convert(models, returnType);
    }

    @SuppressWarnings("unchecked")
    private @Nullable T convert(@NotNull List<EntityModel> models, @NotNull Type returnType) {
        if (returnType == Void.class) return null;
        Class<? extends EntityModel> entityType = getReturnType(returnType);
        List<?> entities = models.stream()
                .map(model -> {
                    try {
                        return ((Object) manager.createEntity(entityType, model));
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        if (returnType instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType() instanceof Class<?> classType) {
                if (classType == List.class) {
                    return (T) entities;
                }
            }
        }
        if (returnType instanceof Class<?> classType) {
            if (entities.size() != 1) {
                throw new ProxyException("Could not convert '" + models + "' into a single entity of '" + classType.getName() + "'");
            }
            return (T) entities.getFirst();
        }
        throw new ProxyException("Could not convert '" + models + "' into '" + returnType + "'");
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
        throw new ProxyException("'" + returnType + "' is not supported");
    }

    private @Nullable List<EntityModel> get(@NotNull HttpResponse<byte[]> response) throws IOException, InterruptedException {
        byte[] body = response.body();
        if (body.length == 0) return null;
        ResponseModel collectionModel = ResponseModel.fromXML(new String(body, StandardCharsets.UTF_8));
        List<EntityModel> models = new ArrayList<>(collectionModel.getEntities());
        onSingleEntity(models, collectionModel);
        collectionModel.getLink("next");
        URI nextLink;
        while ((nextLink = collectionModel.getLink("next")) != null) {
            NetworkQuery<HttpResponse<byte[]>> next = manager.getNetworkClient().newRequest(nextLink).build();
            response = next.get();
            collectionModel = ResponseModel.fromXML(new String(response.body(), StandardCharsets.UTF_8));
            models.addAll(collectionModel.getEntities());
        }
        return models;
    }

    private void onSingleEntity(@NotNull List<EntityModel> models, @NotNull ResponseModel collectionModel) {
        if (collectionModel.getLink("self") != null) {
            for (EntityModel model : models) {
                if (model.getType().endsWith("-li")) continue;
                model.getLinks().putIfAbsent("self", collectionModel.getLink("self"));
            }
        }
    }
}
