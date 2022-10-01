package com.bossymr.rapid.network.client.model;

import com.bossymr.rapid.network.client.annotations.Entity;
import com.bossymr.rapid.network.client.annotations.Field;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public record Model(@NotNull EntityModel entity, @NotNull List<EntityModel> entities) {

    public <T> @NotNull List<T> getEntities(@NotNull Class<T> clazz) {
        List<T> entities = new ArrayList<>();
        for (EntityModel entity : entities()) {
            if(canHandle(entity, clazz)) {
                entities.add(getEntity(entity, clazz));
            }
        }
        return Collections.unmodifiableList(entities);
    }

    public <T> @NotNull T getEntity(@NotNull Class<T> clazz) {
        if (entities().size() != 1) throw new IllegalStateException();
        EntityModel entity = entities().get(0);
        return getEntity(entity, clazz);
    }

    private <T> @NotNull T getEntity(@NotNull EntityModel entity, @NotNull Class<T> clazz) {
        if (!canHandle(entity, clazz)) throw new IllegalArgumentException();
        RecordComponent[] components = clazz.getRecordComponents();
        int size = components.length;
        Class<?>[] arguments = new Class[size];
        Object[] objects = new Object[size];
        for (int i = 0; i < size; i++) {
            RecordComponent component = components[i];
            arguments[i] = component.getType();
            if (component.isAnnotationPresent(Field.class)) {
                Field field = component.getAnnotation(Field.class);
                Optional<Property> optional = entity.getProperty(field.value());
                if (optional.isPresent()) {
                    String value = optional.get().content();
                    if (component.getType().isAssignableFrom(String.class)) {
                        objects[i] = value;
                    } else {
                        throw new IllegalStateException();
                    }
                } else {
                    objects[i] = null;
                }
            } else {
                throw new IllegalStateException();
            }
        }
        try {
            return clazz.getDeclaredConstructor(arguments).newInstance(objects);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean canHandle(@NotNull EntityModel entity, @NotNull Class<?> clazz) {
        if (!clazz.isRecord()) return false;
        if (!clazz.isAnnotationPresent(Entity.class)) return false;
        Entity annotation = clazz.getAnnotation(Entity.class);
        for (String type : annotation.value()) {
            if (entity.type().equals(type)) {
                return true;
            }
        }
        return false;
    }
}
