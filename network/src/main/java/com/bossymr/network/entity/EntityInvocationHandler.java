package com.bossymr.network.entity;

import com.bossymr.network.annotations.Deserializable;
import com.bossymr.network.annotations.Property;
import com.bossymr.network.annotations.Title;
import com.bossymr.network.client.EntityModel;
import com.bossymr.network.client.NetworkManager;
import com.bossymr.network.client.RequestFactory;
import com.bossymr.network.client.ResponseModel;
import com.bossymr.network.client.proxy.EntityProxy;
import com.bossymr.network.client.proxy.NetworkProxy;
import com.bossymr.network.client.proxy.ProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EntityInvocationHandler extends AbstractInvocationHandler {

    private final @NotNull Class<?> type;
    private @Nullable NetworkManager manager;
    private @NotNull EntityModel model;

    public EntityInvocationHandler(@Nullable NetworkManager manager, @NotNull Class<?> type, @NotNull EntityModel model) {
        this.model = model;
        this.manager = manager;
        this.type = type;
    }

    @Override
    public @Nullable Object execute(@NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (isMethod(method, NetworkProxy.class, "getNetworkManager")) {
            return manager;
        }
        if (isMethod(method, NetworkProxy.class, "move", NetworkManager.class)) {
            this.manager = (NetworkManager) args[0];
            return null;
        }
        if (isMethod(method, EntityProxy.class, "refresh")) {
            if (model.reference("self") == null) {
                throw new ProxyException("Could not refresh");
            }
            getSelf();
        }
        if (isMethod(method, EntityProxy.class, "getProperty", String.class)) {
            return getProperty((String) args[0]);
        }
        if (isMethod(method, EntityProxy.class, "getReference", String.class)) {
            return getReference((String) args[0]);
        }
        if (isMethod(method, EntityProxy.class, "getModel")) {
            return model;
        }
        if (method.isAnnotationPresent(Title.class)) {
            return model.title();
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
            throw new IllegalStateException("Entity is not managed");
        }
        return new RequestFactory(manager).createQuery(type, proxy, method, args);
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

    private @Nullable URI getReference(@NotNull String type) {
        URI reference = model.reference(type);
        if (reference != null) {
            return reference;
        }
        if (model.type().endsWith("-li")) {
            return getSelf().reference(type);
        }
        return null;
    }

    private @Nullable String getProperty(@NotNull String type) {
        String field = model.property(type);
        if (field != null) {
            return field;
        }
        if (model.type().endsWith("-li")) {
            return getSelf().property(type);
        }
        return null;
    }

    private @NotNull EntityModel getSelf() {
        if (manager == null) {
            throw new IllegalStateException("Entity is not managed");
        }
        URI reference = model.reference("self");
        if (reference == null) {
            throw new ProxyException("Entity '" + model + "' has no reference to itself");
        }
        HttpRequest httpRequest = HttpRequest.newBuilder(reference).build();
        try {
            HttpResponse<byte[]> response = manager.getNetworkClient().send(httpRequest);
            ResponseModel collectionModel = ResponseModel.convert(response.body());
            if (collectionModel.entities().size() != 1) {
                throw new ProxyException("Request to self reference '" + reference + "' responded with multiple entities");
            }
            return model = collectionModel.entities().get(0);
        } catch (IOException | InterruptedException e) {
            throw new ProxyException(e);
        }
    }

    @Override
    public boolean equals(@NotNull Object proxy, @NotNull Object obj) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(obj);
        if (!(invocationHandler instanceof EntityInvocationHandler entity)) return false;
        return entity.type.equals(type) && entity.model.equals(model) && Objects.equals(entity.manager, manager);
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
        return type.getName() + ":" + model;
    }
}
