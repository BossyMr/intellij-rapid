package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;

/**
 * A {@code GenericType} represents a generic type {@code <T>}.
 * <p>
 * This class needs to be subclassed with the specific generic type.
 * <p>
 * {@code GenericType<List<String>> genericType = new GenericType<>() {}}
 *
 * @param <T> the generic type.
 */
public abstract class GenericType<T> {

    private final Type type;

    public GenericType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException();
        }
        type = parameterizedType.getActualTypeArguments()[0];
    }

    private GenericType(@NotNull Type type) {
        this.type = type;
    }

    public static @NotNull GenericType<?> of(@NotNull Type type) {
        return new NonGenericType(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> @NotNull GenericType<T> of(@NotNull Class<T> type) {
        return (GenericType<T>) new NonGenericType(type);
    }

    /**
     * Calculates the outermost type. For example, for {@code List<String>}, the outermost type is {@code List}.
     * Likewise, for {@code String[]}, the outermost type is {@code String}.
     *
     * @param type the type.
     * @return the outermost type of the specified type.
     */
    private static @NotNull Class<?> getRawType(@NotNull Type type) {
        if (type instanceof Class<?> classType) {
            return classType;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            if (!(parameterizedType.getRawType() instanceof Class<?> classType)) {
                throw new IllegalStateException();
            }
            return classType;
        }
        if (type instanceof GenericArrayType genericArrayType) {
            Type componentType = genericArrayType.getGenericComponentType();
            return Array.newInstance(getRawType(componentType), 0).getClass();
        }
        if (type instanceof TypeVariable<?>) {
            return Object.class;
        }
        if (type instanceof WildcardType wildcardType) {
            assert wildcardType.getUpperBounds().length == 1;
            return getRawType(wildcardType.getUpperBounds()[0]);
        }
        throw new IllegalArgumentException();
    }

    public @NotNull Type getType() {
        return type;
    }

    public @NotNull Class<?> getRawType() {
        return getRawType(getType());
    }

    private static final class NonGenericType extends GenericType<Object> {

        public NonGenericType(@NotNull Type type) {
            super(type);
        }
    }
}
