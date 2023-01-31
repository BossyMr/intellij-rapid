package com.bossymr.network.entity;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Deserializable;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.model.CollectionModel;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EntityInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull NetworkEngine engine;
    private @NotNull Model model;

    public EntityInvocationHandler(@NotNull NetworkEngine engine, @NotNull Model model) {
        this.model = model;
        this.engine = engine;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, EntityModel.class, "getNetworkEngine")) {
            return engine;
        }
        if (isMethod(method, EntityModel.class, "getTitle")) {
            return model.getTitle();
        }
        if (isMethod(method, EntityModel.class, "getType")) {
            return model.getType();
        }
        if (isMethod(method, EntityModel.class, "getLinks")) {
            return model.getLinks();
        }
        if (isMethod(method, EntityModel.class, "getFields")) {
            return model.getFields();
        }
        if (isMethod(method, EntityModel.class, "getNetworkFactory")) {
            return engine;
        }
        if (isMethod(method, EntityModel.class, "getLink", String.class)) {
            return getLink((String) args[0]);
        }
        if (isMethod(method, EntityModel.class, "getField", String.class)) {
            return getField((String) args[0]);
        }
        if (method.isAnnotationPresent(Property.class)) {
            Property property = method.getAnnotation(Property.class);
            String name = property.value();
            String value = getField(name);
            if (value != null) {
                return convert(value, method.getReturnType());
            } else {
                return null;
            }
        }
        return engine.getRequestFactory().createQuery(proxy, method, args);
    }

    private @NotNull Object convert(@NotNull String value, @NotNull Class<?> type) throws IllegalAccessException {
        type = getBoxType(type);
        if (type == String.class) return value;
        if (type == Byte.class) return Byte.parseByte(value);
        if (type == Short.class) return Short.parseShort(value);
        if (type == Integer.class) return Integer.parseInt(value);
        if (type == Long.class) return Long.parseLong(value);
        if (type == Float.class) return Float.parseFloat(value);
        if (type == Double.class) return Double.parseDouble(value);
        if (type == Boolean.class) return Boolean.parseBoolean(value);
        if (type == Character.class) return value.charAt(0);
        if (type == LocalDateTime.class) {
            return LocalDateTime.parse(value.replaceAll(" ", ""), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        if (Enum.class.isAssignableFrom(type)) {
            Map<String, Object> constants = new HashMap<>();
            for (Field field : type.getFields()) {
                if (field.isEnumConstant()) {
                    Deserializable deserializable = field.getAnnotation(Deserializable.class);
                    String name;
                    if (deserializable != null) {
                        name = deserializable.value();
                    } else {
                        name = field.getName();
                        throw new IllegalArgumentException("Enum Constant '" + name + "' of '" + type + "' is not annotated as deserializable");
                    }
                    if (constants.containsKey(name)) {
                        throw new IllegalArgumentException("Enum contains duplicate constant '" + name + "'");
                    }
                    constants.put(name, field.get(null));
                }
            }
            if (constants.containsKey(value)) {
                return constants.get(value);
            }
        }
        throw new IllegalStateException("Unable to convert '" + value + "' into '" + type + "'");
    }

    private @NotNull Class<?> getBoxType(@NotNull Class<?> type) {
        if (!(type.isPrimitive())) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private @Nullable URI getLink(@NotNull String type) {
        URI path = model.getLink(type);
        if (path != null) {
            return path;
        }
        if (fetch()) {
            return model.getLink(type);
        }
        return null;
    }

    private @Nullable String getField(@NotNull String type) {
        String field = model.getField(type);
        if (field != null) {
            return field;
        }
        if (fetch()) {
            return model.getField(type);
        }
        return null;
    }

    private boolean fetch() {
        if (model.getLink("self") == null) {
            return false;
        }
        HttpRequest httpRequest = HttpRequest.newBuilder(model.getLink("self")).build();
        try {
            HttpResponse<byte[]> response = engine.getNetworkClient().send(httpRequest);
            CollectionModel collectionModel = CollectionModel.convert(response.body());
            if (collectionModel.getModels().size() != 1) {
                return false;
            }
            Model updated = collectionModel.getModels().get(0);
            String previous = model.getType().substring(0, model.getType().length() - "-li".length());
            if (updated.getType().equals(previous)) {
                model = updated;
                return true;
            }
        } catch (IOException | InterruptedException e) {
            return false;
        }
        return false;
    }
}
