package com.bossymr.rapid.robot.api.entity;

import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.NetworkTarget;
import com.bossymr.rapid.robot.api.NetworkType;
import com.bossymr.rapid.robot.api.annotations.Alias;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import com.bossymr.rapid.robot.api.annotations.Title;
import com.bossymr.rapid.robot.api.client.RequestFactory;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.bossymr.rapid.robot.api.client.proxy.EntityProxy;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull Class<?> type;
    private @NotNull EntityModel model;

    public EntityInvocationHandler(@Nullable NetworkManager manager, @NotNull Class<?> type, @NotNull EntityModel model) {
        this.model = model;
        this.manager = manager;
        this.type = type;
    }

    public static <T> @NotNull T createEntity(@Nullable NetworkManager manager, @NotNull Class<T> entityType, @NotNull EntityModel model) {
        Class<? extends T> actualType = getEntityType(entityType, model);
        if (actualType == null) {
            throw new IllegalArgumentException(model.getType() + " could not be converted into " + entityType.getName());
        }
        Class<? extends T> virtualType = new ByteBuddy()
                .subclass(actualType)
                .implement(EntityProxy.class)
                .method(ElementMatchers.any())
                .intercept(InvocationHandlerAdapter.of(new EntityInvocationHandler(manager, actualType, model)))
                .make()
                .load(NetworkManager.class.getClassLoader())
                .getLoaded();
        try {
            Constructor<? extends T> constructor = virtualType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("could not create entity: entity '" + entityType + "' must define a constructor with no arguments", e);
        }
    }

    private static <T> @Nullable Class<? extends T> getEntityType(@NotNull Class<T> entityType, @NotNull EntityModel model) {
        Map<String, Class<? extends T>> entities = getEntityGraph(entityType);
        String type = model.getType();
        if (type.endsWith("-li")) {
            return entities.get(type.substring(0, type.length() - "-li".length()));
        } else {
            return entities.get(type);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> @NotNull Map<String, Class<? extends T>> getEntityGraph(@NotNull Class<? extends T> entityType) {
        Map<String, Class<? extends T>> entities = new HashMap<>();
        Entity entity = entityType.getAnnotation(Entity.class);
        if (entity == null) {
            throw new IllegalArgumentException("Entity '" + entityType + "' must be annotated with Entity");
        }
        for (String type : entity.value()) {
            if (entities.containsKey(type)) {
                throw new IllegalArgumentException(entityType.getName() + " (" + type + ") is declared more than once");
            }
            entities.put(type, entityType);
        }
        for (Class<?> subtype : entity.subtype()) {
            if (entityType.equals(subtype)) {
                throw new IllegalArgumentException(subtype.getName() + " cannot be declared as subtype of itself");
            }
            if (!(entityType.isAssignableFrom(subtype))) {
                throw new IllegalArgumentException(subtype.getName() + " does not implement supertype " + entityType.getName());
            }
            Map<String, Class<? extends T>> graph = getEntityGraph((Class<? extends T>) subtype);
            entities.putAll(graph);
        }
        return entities;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, EntityProxy.class, "refresh")) {
            if (model.getLink("self") == null) {
                throw new ProxyException("could not refresh entity: no 'self' link");
            }
            getSelf();
        }
        if (isMethod(method, EntityProxy.class, "getModel")) {
            return model;
        }
        if (method.isAnnotationPresent(Title.class)) {
            return model.getTitle();
        }
        if (method.isAnnotationPresent(Property.class)) {
            Property property = method.getAnnotation(Property.class);
            String[] names = property.value();
            for (String name : names) {
                if (name.startsWith("{") && name.endsWith("}")) {
                    name = name.substring(1, name.length() - 1);
                    if (name.startsWith("@")) {
                        URI reference = getReference(name.substring(1));
                        if (reference != null) {
                            return convert(reference.toString(), method.getReturnType());
                        }
                    }
                    if (name.startsWith("#")) {
                        String value = getProperty(name.substring(1));
                        if (value != null) {
                            return convert(value, method.getReturnType());
                        }
                    }
                } else {
                    String value = getProperty(name);
                    if (value != null) {
                        return convert(value, method.getReturnType());
                    }
                }
            }
            return null;
        }
        if (manager == null) {
            throw new ProxyException("could not invoke method '" + method.getName() + "': entity is not managed");
        }
        return new RequestFactory(manager).createQuery(type, proxy, method, args);
    }

    private @NotNull Object convert(@NotNull String value, @NotNull Class<?> type) throws IllegalAccessException {
        if (type == String.class) return value;
        if (type == byte.class || type == Byte.class) return Byte.parseByte(value);
        if (type == short.class || type == Short.class) return Short.parseShort(value);
        if (type == int.class || type == Integer.class) return Integer.parseInt(value);
        if (type == long.class || type == Long.class) return Long.parseLong(value);
        if (type == float.class || type == Float.class) return Float.parseFloat(value);
        if (type == double.class || type == Double.class) return Double.parseDouble(value);
        if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == char.class || type == Character.class) return value.charAt(0);
        if (type == URI.class) {
            try {
                return URI.create(value);
            } catch (IllegalArgumentException e) {
                throw new ProxyException(e);
            }
        }
        if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value.replaceAll(" ", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (Enum.class.isAssignableFrom(type)) {
            Map<String, Object> constants = new HashMap<>();
            for (Field field : type.getFields()) {
                if (field.isEnumConstant()) {
                    getField(field, constants);
                }
            }
            if (constants.containsKey(value)) {
                return constants.get(value);
            }
        }
        throw new IllegalStateException("Unable to convert '" + value + "' into '" + type + "'");
    }

    private void getField(@NotNull Field field, Map<String, Object> constants) throws IllegalAccessException {
        Alias alias = field.getAnnotation(Alias.class);
        if (alias != null) {
            for (String value : alias.value()) {
                if (constants.containsKey(value)) {
                    throw new IllegalArgumentException("Enum contains duplicate constant '" + value + "'");
                }
                constants.put(value, field.get(null));
            }
        } else {
            String name = field.getName();
            if (constants.containsKey(name)) {
                throw new IllegalArgumentException("Enum contains duplicate constant '" + name + "'");
            }
            constants.put(name, field.get(null));
        }
    }

    private @Nullable URI getReference(@NotNull String type) {
        URI reference = model.getLink(type);
        if (reference != null) {
            return reference;
        }
        if (model.getType().endsWith("-li")) {
            return getSelf().getLink(type);
        }
        return null;
    }

    private @Nullable String getProperty(@NotNull String type) {
        String field = model.getProperty(type);
        if (field != null) {
            return field;
        }
        if (model.getType().endsWith("-li")) {
            return getSelf().getProperty(type);
        }
        return null;
    }

    private @NotNull EntityModel getSelf() {
        if (manager == null) {
            throw new IllegalStateException("Entity is not managed");
        }
        URI reference = model.getLink("self");
        if (reference == null) {
            throw new ProxyException("Entity '" + model + "' has no reference to itself");
        }
        try {
            NetworkTarget<ResponseModel> target = NetworkTarget.newTarget(reference, NetworkType.modelType()).build();
            ResponseModel collectionModel = manager.createQuery(target).get();
            if (collectionModel.getEntities().size() != 1) {
                throw new ProxyException("Request to self reference '" + reference + "' responded with multiple entities");
            }
            return model = collectionModel.getEntities().getFirst();
        } catch (IOException | InterruptedException e) {
            throw new ProxyException(e);
        }
    }

    @Override
    public boolean equals(@NotNull Object proxy, @NotNull Object obj) {
        if (!(obj instanceof EntityProxy entity)) return false;
        if (!(type.isInstance(obj))) return false;
        return entity.getModel().equals(model) && Objects.equals(entity.getNetworkManager(), manager);
    }

    @Override
    public int hashCode(@NotNull Object proxy) {
        int result = type.hashCode();
        result = 31 * result + (manager != null ? manager.hashCode() : 0);
        result = 31 * result + model.hashCode();
        return result;
    }

    @Override
    public String toString(@NotNull Object proxy) {
        return type.getSimpleName() + ":" + model;
    }
}
