package com.bossymr.rapid.robot.api;

import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.bossymr.rapid.robot.api.client.proxy.ListProxy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@code NetworkType} represents the return type of a network request, as-well as how the response should be
 * converted into the appropriate type.
 *
 * @param <T> the return type of the network request.
 */
public abstract class NetworkType<T> {

    private static <T> @NotNull NetworkType<T> customType(@NotNull TransformFunction<HttpResponse<byte[]>, T> transform) {
        return new NetworkType<>() {
            @Override
            public T convert(@NotNull NetworkManager manager, @NotNull HttpResponse<byte[]> response) throws IOException {
                return transform.convert(manager, response);
            }
        };
    }

    /**
     * {@return a NetworkType that will discard the response}
     */
    public static @NotNull NetworkType<Void> voidType() {
        return customType((manager, response) -> null);
    }

    /**
     * {@return a NetworkType that will return the response as-is}
     */
    public static @NotNull NetworkType<HttpResponse<byte[]>> rawType() {
        return customType((manager, response) -> response);
    }

    /**
     * {@return a NetworkType that will return the response formatted as a string}
     */
    public static @NotNull NetworkType<String> stringType() {
        return customType((manager, response) -> new String(response.body(), StandardCharsets.UTF_8));
    }

    /**
     * {@return a NetworkType that will return the response as a ResponseModel}
     */
    public static @NotNull NetworkType<ResponseModel> modelType() {
        return stringType().transform((manager, body) -> {
            Pattern pattern = Pattern.compile("\"([^\"]*)\"");
            Matcher matcher = pattern.matcher(body);
            body = matcher.replaceAll(result -> result.group()
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;"));
            return ResponseModel.fromXML(body);
        });
    }

    /**
     * Returns a {@code NetworkType} that will return the response as an entity.
     *
     * @param entityType the type of entity.
     * @param <T> the type of entity.
     * @return a {@code NetworkType}.
     */
    public static <T> @NotNull NetworkType<T> entityType(Class<T> entityType) {
        return modelType().transform((manager, model) -> {
            List<T> entities = model.getEntities().stream()
                    .map(entity -> {
                        try {
                            return manager.createEntity(entityType, entity);
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull)
                    .toList();
            if (entities.size() != 1) {
                throw new IOException("could not convert '" + model + "' into type '" + entityType + "'");
            }
            return entities.getFirst();
        });
    }

    /**
     * Returns a {@code NetworkType} that will return the response as a list of entities.
     *
     * @param entityType the type of entity.
     * @param <T> the type of entity.
     * @return a {@code NetworkType}.
     */
    public static <T> @NotNull NetworkType<List<T>> listType(Class<T> entityType) {
        return modelType().transform((manager, model) -> new ListProxy<>(manager, entityType, model));
    }

    /**
     * Attempts to convert the specified response into this type.
     *
     * @param manager the manager.
     * @param response the response.
     * @return a new instance of this type.
     * @throws IOException if the response could not be converted into this type.
     */
    public abstract T convert(@NotNull NetworkManager manager, @NotNull HttpResponse<byte[]> response) throws IOException;

    /**
     * Transforms this {@code NetworkType} into another {@code NetworkType} by passing the return value computed by this
     * type into the provided function.
     *
     * @param transform a function.
     * @param <E> the return type of the new {@code NetworkType}.
     * @return a new {@code NetworkType}.
     */
    public <E> @NotNull NetworkType<E> transform(@NotNull TransformFunction<T, E> transform) {
        return new NetworkType<>() {
            @Override
            public E convert(@NotNull NetworkManager manager, @NotNull HttpResponse<byte[]> response) throws IOException {
                return transform.convert(manager, NetworkType.this.convert(manager, response));
            }
        };
    }

    /**
     * A function that will transform a value into another value.
     *
     * @param <T> the type of the original value.
     * @param <E> the type of the transformed value.
     */
    @FunctionalInterface
    public interface TransformFunction<T, E> {
        /**
         * Converts a value from the original to the transformed type.
         *
         * @param manager the manager.
         * @param value the original value.
         * @return the transformed value.
         * @throws IOException if the value could not be transformed.
         */
        E convert(@NotNull NetworkManager manager, @NotNull T value) throws IOException;
    }
}
