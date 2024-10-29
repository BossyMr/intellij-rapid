package com.bossymr.rapid.robot.api;

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

    /**
     * Creates a new {@code GenericType}.
     */
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

    public static @NotNull GenericType<Void> voidType() {
        return GenericType.of(Void.class);
    }

    /**
     * Creates a new {@code GenericType} representing the specified type.
     *
     * @param type the type.
     * @return a {@code GenericType} representing the specified type.
     */
    public static @NotNull GenericType<?> of(@NotNull Type type) {
        return new NonGenericType(type);
    }

    /**
     * Creates a new {@code GenericType} representing the specified type.
     *
     * @param type the type.
     * @param <T> the type.
     * @return a {@code GenericType} representing the specified type.
     */
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

    /**
     * Returns the type of this {@code GenericType}.
     *
     * @return the type of this {@code GenericType}.
     */
    public @NotNull Type getType() {
        return type;
    }

    /**
     * Returns the outermost type of this {@code GenericType}.
     * <p>
     * The outermost type is the least specific type. For example, the outermost type of {@code List<String>} is
     * {@code List}. Likewise, the outermost type of {@code String[]} is {@code String}.
     *
     * @return the outermost type of this {@code GenericType}.
     */
    public @NotNull Class<?> getRawType() {
        return getRawType(getType());
    }

    private static final class NonGenericType extends GenericType<Object> {

        public NonGenericType(@NotNull Type type) {
            super(type);
        }
    }
}
