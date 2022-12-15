package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.Link;
import com.bossymr.rapid.robot.network.annotations.Deserializable;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.client.impl.AsynchronousQueryImpl;
import com.bossymr.rapid.robot.network.client.impl.QueryImpl;
import com.bossymr.rapid.robot.network.client.impl.SubscribableQueryImpl;
import com.bossymr.rapid.robot.network.query.AsynchronousQuery;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class NetworkUtil {

    private static final Logger LOG = Logger.getInstance(NetworkUtil.class);

    private NetworkUtil() {
        throw new UnsupportedOperationException("'NetworkUtil' cannot be instantiated");
    }

    public static @NotNull Object newQuery(@NotNull Class<?> type, @NotNull NetworkClient networkClient, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        if (method.getReturnType().isAnnotationPresent(Service.class)) {
            return networkClient.newService(method.getReturnType());
        }
        Service service = type.getAnnotation(Service.class);
        String path = service != null ? service.value() : "";
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof Query.GET request) {
                return newQuery(networkClient, proxy, "GET", path + request.value(), method, args);
            }
            if (annotation instanceof Query.POST request) {
                return newQuery(networkClient, proxy, "POST", path + request.value(), method, args);
            }
            if (annotation instanceof Query.PUT request) {
                return newQuery(networkClient, proxy, "PUT", path + request.value(), method, args);
            }
            if (annotation instanceof Query.DELETE request) {
                return newQuery(networkClient, proxy, "DELETE", path + request.value(), method, args);
            }
            if (annotation instanceof AsynchronousQuery.Asynchronous request) {
                return newAsynchronousQuery(networkClient, proxy, path + request.value(), method, args);
            }
            if (annotation instanceof SubscribableQuery.Subscribable request) {
                return newSubscribableQuery(networkClient, proxy, request.value(), method, args);
            }
        }
        LOG.error("Cannot handle method '" + method.getName() + "' in '" + type.getName() + "'");
        throw new AssertionError();
    }

    public static @NotNull Query<?> newQuery(@NotNull NetworkClient networkClient, @NotNull Object proxy, @NotNull String command, @NotNull String path, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        HttpRequest request = getRequest(networkClient, proxy, command, path, method, args);
        return new QueryImpl<>(networkClient, request, ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0]);
    }

    public static @NotNull AsynchronousQuery newAsynchronousQuery(@NotNull NetworkClient networkClient, @NotNull Object proxy, @NotNull String path, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        HttpRequest request = getRequest(networkClient, proxy, "POST", path, method, args);
        return new AsynchronousQueryImpl(networkClient, request);
    }

    private static HttpRequest getRequest(@NotNull NetworkClient networkClient, @NotNull Object proxy, @NotNull String command, @NotNull String path, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        String interpolate = interpolate(path, proxy, method, args);
        URI address = networkClient.getPath().resolve(interpolate);
        Map<String, String> queries = collect(method, args, annotation -> annotation instanceof Query.Argument field ? field.value() : null, Query.ArgumentMap.class);
        if (queries.size() > 0) {
            String query = (address.getPath() != null ? "&" : "?") + queries.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            address = networkClient.getPath().resolve(interpolate + query);
        }
        Map<String, String> fields = collect(method, args, annotation -> annotation instanceof Query.Field field ? field.value() : null, Query.FieldMap.class);
        String field = fields.size() > 0 ? fields.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&")) : null;
        HttpRequest.Builder builder = HttpRequest.newBuilder(address)
                .method(command, field != null ? HttpRequest.BodyPublishers.ofString(field) : HttpRequest.BodyPublishers.noBody());
        if (field != null) builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
        return builder.build();
    }

    public static @NotNull SubscribableQuery<?> newSubscribableQuery(@NotNull NetworkClient networkClient, @NotNull Object proxy, @NotNull String path, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        if (method.getParameterCount() > 0) {
            LOG.warn("Method '" + method.getName() + "' in '" + method.getDeclaringClass().getName() + "' should not contain parameters");
        }
        @SuppressWarnings("unchecked")
        Class<? extends EntityModel> returnType = (Class<? extends EntityModel>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        return new SubscribableQueryImpl<>(networkClient, interpolate(path, proxy, method, args), returnType);
    }

    public static @NotNull String interpolate(@NotNull String path, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        Map<String, String> map = collect(method, args, annotation -> annotation instanceof Query.Path argument ? argument.value() : null, Query.PathMap.class);
        return Pattern.compile("\\{([^}]*)}").matcher(path)
                .replaceAll(result -> {
                    String value = result.group().substring(1, result.group().length() - 1);
                    if (value.startsWith("@")) {
                        if (proxy instanceof EntityModel model) {
                            Link link = model.getLink(value.substring(1));
                            if (link != null) {
                                return link.path().getPath();
                            } else {
                                LOG.error("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' points to missing link '" + value + "'");
                            }
                        } else {
                            LOG.error("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' cannot point to a link");
                        }
                    }
                    if (map.containsKey(value)) {
                        return map.get(value);
                    }
                    LOG.error("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' does not provide value for '" + value + "'");
                    throw new AssertionError();
                });
    }

    public static @NotNull Map<String, String> collect(@NotNull Method method, Object @NotNull [] args, @NotNull Function<Annotation, String> function, @NotNull Class<? extends Annotation> marker) throws NoSuchFieldException {
        Map<String, String> map = new HashMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (marker.isInstance(annotation)) {
                    if (args[i] instanceof Map<?, ?> values) {
                        for (Map.Entry<?, ?> entry : values.entrySet()) {
                            map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                        }
                    } else {
                        LOG.error("Parameter of '" + method.getName() + "' should be Map<String, String>");
                    }
                }
                String value = function.apply(annotation);
                if (value != null) {
                    map.put(value, convert(args[i]));
                }
            }
        }
        return map;
    }

    public static @NotNull String convert(@NotNull Object object) throws NoSuchFieldException {
        if (object instanceof Enum<?> enumerated) {
            Field field = object.getClass().getField(enumerated.name());
            Deserializable annotation = field.getAnnotation(Deserializable.class);
            return annotation != null ? annotation.value() : enumerated.name();
        }
        return String.valueOf(object);
    }

    public static @NotNull Object convert(@NotNull String value, @NotNull Class<?> type) throws IllegalAccessException {
        if (String.class.isAssignableFrom(type)) return value;
        if (Byte.class.isAssignableFrom(type)) return Byte.parseByte(value);
        if (Short.class.isAssignableFrom(type)) return Short.parseShort(value);
        if (Integer.class.isAssignableFrom(type)) return Integer.parseInt(value);
        if (Long.class.isAssignableFrom(type)) return Long.parseLong(value);
        if (Float.class.isAssignableFrom(type)) return Float.parseFloat(value);
        if (Double.class.isAssignableFrom(type)) return Double.parseDouble(value);
        if (Boolean.class.isAssignableFrom(type)) return Boolean.parseBoolean(value);
        if (Character.class.isAssignableFrom(type)) return value.charAt(0);
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
                        LOG.warn("Enum Constant '" + name + "' of '" + type + "' is not annotated as deserializable");
                    }
                    if (constants.containsKey(name)) {
                        LOG.error("Enum contains duplicate constant '" + name + "'");
                    }
                    constants.put(name, field.get(null));
                }
            }
        }
        throw new IllegalStateException("Unable to convert '" + value + "' into '" + type + "'");
    }
}
