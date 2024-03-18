package com.bossymr.rapid.robot.api.client.response;

import com.bossymr.rapid.robot.api.GenericType;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.ResponseConverter;
import com.bossymr.rapid.robot.api.ResponseConverterFactory;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.client.EntityModel;
import com.bossymr.rapid.robot.api.client.ResponseModel;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    public @Nullable T convert(@NotNull Response response) throws IOException, InterruptedException {
        return convert(response, type.getType());
    }

    public @Nullable T convert(@NotNull EntityModel model) {
        return convert(List.of(model), type.getType());
    }

    private @Nullable T convert(@NotNull Response response, @NotNull Type returnType) throws IOException, InterruptedException {
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
            return (T) entities.get(0);
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

    private @Nullable List<EntityModel> get(@NotNull Response response) throws IOException, InterruptedException {
        byte[] body = response.body().bytes();
        response.close();
        if (body.length == 0) return null;
        ResponseModel collectionModel = ResponseModel.convert(body);
        List<EntityModel> models = new ArrayList<>(collectionModel.entities());
        onSingleEntity(models, collectionModel);
        while (collectionModel.model().reference("next") != null) {
            Request next = new Request.Builder(response.request())
                    .url(Objects.requireNonNull(collectionModel.model().reference("next")).toURL())
                    .build();
            try (@NotNull Response closeable = manager.getNetworkClient().send(next)) {
                response = closeable;
                collectionModel = ResponseModel.convert(response.body().bytes());
            }
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
