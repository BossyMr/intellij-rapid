package com.bossymr.network;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;

public abstract class GenericType<T> implements Comparable<T> {

    private final @NotNull Type type;

    public GenericType() {
        Type superclass = getClass().getGenericSuperclass();
        if (!(superclass instanceof ParameterizedType parameterizedType)) {
            throw new IllegalStateException();
        }
        type = parameterizedType.getActualTypeArguments()[0];
    }

    public static <T> @NotNull GenericType<T> of(@NotNull Type type) {
        return new GenericType<>() {
            @Override
            public @NotNull Type getType() {
                return type;
            }
        };
    }

    public static <T> @NotNull GenericType<T> of(@NotNull Class<T> type) {
        return of(((Type) type));
    }

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

    @Override
    public int compareTo(@NotNull T o) {
        return 0;
    }

    @Override
    public String toString() {
        return "GenericType{" +
                "type=" + getType() +
                '}';
    }
}
