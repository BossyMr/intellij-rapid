package com.bossymr.network.client.response;

import com.bossymr.network.ResponseConverter;
import com.bossymr.network.ResponseConverterFactory;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.client.EntityModel;
import com.bossymr.network.client.GenericType;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.ResponseModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class EntityConverter<T> implements ResponseConverter<T> {

    public static final ResponseConverterFactory FACTORY = new ResponseConverterFactory() {
        @Override
        public <E> ResponseConverter<E> create(@NotNull NetworkManager manager, @NotNull HttpResponse<?> response, @NotNull GenericType<E> type) {
            Optional<String> optional = response.headers().firstValue("Content-Type");
            if (optional.isEmpty() || !(optional.orElseThrow().equals("application/xhtml+xml"))) {
                return null;
            }
            return new EntityConverter<>(manager, type);
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
            throw new IllegalArgumentException("'" + entityType.getName() + "' is not annotated with Entity");
        }
    }

    @Override
    public @Nullable T convert(@NotNull HttpResponse<byte[]> response) throws IOException, InterruptedException {
        return convert(response, type.getType());
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

    private @Nullable List<EntityModel> get(@NotNull HttpResponse<byte[]> response) throws IOException, InterruptedException {
        byte[] body = response.body();
        if (body.length == 0) return null;
        ResponseModel collectionModel = ResponseModel.convert(body);
        List<EntityModel> models = new ArrayList<>(collectionModel.entities());
        onSingleEntity(models, collectionModel);
        while (collectionModel.model().reference("next") != null) {
            HttpRequest next = HttpRequest.newBuilder(response.request(), (n, v) -> true)
                    .uri(collectionModel.model().reference("next"))
                    .build();
            response = manager.getNetworkClient().send(next);
            collectionModel = ResponseModel.convert(response.body());
            models.addAll(collectionModel.entities());
        }
        return models;
    }

    private void onSingleEntity(@NotNull List<EntityModel> models, @NotNull ResponseModel collectionModel) {
        if (collectionModel.model().reference("self") != null) {
            for (EntityModel model : models) {
                if (model.type().endsWith("-li")) continue;
                model.references().putIfAbsent("self", collectionModel.model().reference("self"));
            }
        }
    }
}
