package com.bossymr.network.client;

import com.bossymr.network.MultiMap;
import com.bossymr.network.NetworkQuery;
import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.*;
import com.bossymr.network.client.proxy.EntityProxy;
import com.bossymr.network.client.proxy.ProxyException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RequestFactory {

    private final @NotNull NetworkManager manager;

    public RequestFactory(@NotNull NetworkManager manager) {
        this.manager = manager;
    }

    public @Nullable Object createQuery(@NotNull Class<?> type, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        if (method.getReturnType().isAnnotationPresent(Service.class)) {
            Class<?> returnType = method.getReturnType();
            if (returnType.isAnnotationPresent(Service.class)) {
                return manager.createService((returnType));
            } else {
                throw new ProxyException();
            }
        }
        Service service = type.getAnnotation(Service.class);
        String path = service != null ? service.value() : "";
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof Fetch request) {
                try {
                    Object result = createNetworkCall(request.method().name(), path + request.value(), request.arguments(), proxy, method, args).get();
                    if (result == null || method.getReturnType().isInstance(result)) {
                        return result;
                    }
                    throw new ProxyException("Method '" + method + "' should return '" + method.getReturnType() + "' but responds with value '" + result + "' of type '" + result.getClass().getName());
                } catch (IOException e) {
                    throw new ProxyException(e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new ProxyException(e);
                }
            }
            if (annotation instanceof Subscribable request) {
                if (method.getReturnType().isAssignableFrom(method.getReturnType())) {
                    return createSubscribableNetworkQuery(request.value(), proxy, method, args);
                }
                throw new ProxyException("Method '" + method + "' is annotated as '@Subscribable' but should return '" + method.getReturnType() + "' - must return '" + SubscribableNetworkQuery.class.getName() + "'");
            }
        }
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        throw new ProxyException("Cannot handle method '" + method.getName() + "' in '" + type.getName() + "'");
    }

    private @NotNull NetworkQuery<?> createNetworkCall(@NotNull String command, @NotNull String path, @NotNull String[] arguments, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        MultiMap<String, String> collected = collect(method, args, annotation -> annotation instanceof Argument argument ? argument.value() : null);
        for (String argument : arguments) {
            String key = argument.split("=")[0];
            String value;
            if (argument.contains("=")) {
                value = argument.substring(key.length() + 1);
            } else {
                value = null;
            }
            collected.add(key, value);
        }
        HttpRequest request = manager.getNetworkClient().createRequest()
                .setMethod(command)
                .setPath(URI.create(interpolate(path, proxy, method, args)))
                .setFields(collect(method, args, annotation -> annotation instanceof Field field ? field.value() : null))
                .setArguments(collected)
                .build();
        return manager.createQuery(GenericType.of(method.getGenericReturnType()), request);
    }

    private @NotNull SubscribableNetworkQuery<?> createSubscribableNetworkQuery(@NotNull String path, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        Class<?> returnType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        SubscribableEvent<?> event = new SubscribableEvent<>(URI.create(interpolate(path, proxy, method, args)), returnType);
        return manager.createSubscribableQuery(event);
    }

    private @NotNull String interpolate(@NotNull String path, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        MultiMap<String, String> map = collect(method, args, annotation -> annotation instanceof Path argument ? argument.value() : null);
        return Pattern.compile("\\{([^}]*)}").matcher(path)
                .replaceAll(result -> {
                    String value = result.group().substring(1, result.group().length() - 1);
                    if (value.startsWith("@")) {
                        if (!(proxy instanceof EntityProxy model)) {
                            throw new ProxyException("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' cannot point to a link");
                        }
                        URI link = model.getReference(value.substring(1));
                        if (link == null) {
                            throw new ProxyException("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' points to missing link '" + value + "'");
                        }
                        String query = link.getQuery();
                        return link.getPath() + (query != null ? "?" + query : "");
                    }
                    if (value.startsWith("#")) {
                        if (!(proxy instanceof EntityProxy model)) {
                            throw new ProxyException("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' cannot point to a field");
                        }
                        String field = model.getProperty(value.substring(1));
                        if (field == null) {
                            throw new ProxyException("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' points to missing field '" + value + "'");
                        }
                        return field;
                    }
                    if (map.containsKey(value)) {
                        return map.first(value);
                    }
                    throw new ProxyException("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' does not provide value for '" + value + "'");
                });
    }

    private @NotNull MultiMap<String, String> collect(@NotNull Method method, Object @NotNull [] args, @NotNull Function<Annotation, String> function) throws NoSuchFieldException {
        MultiMap<String, String> map = new MultiMap<>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                String name = function.apply(annotation);
                if (name == null) continue;
                if (name.isEmpty()) {
                    if (args[i] instanceof Map<?, ?> values) {
                        for (Map.Entry<?, ?> entry : values.entrySet()) {
                            map.add(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                        }
                    } else {
                        throw new ProxyException("Parameter of '" + method.getName() + "' should be Map<String, String>");
                    }
                } else {
                    map.add(name, convert(args[i]));
                }
            }
        }
        return map;
    }

    private @NotNull String convert(@NotNull Object object) throws NoSuchFieldException {
        if (object instanceof Enum<?> enumerated) {
            var field = object.getClass().getField(enumerated.name());
            Deserializable annotation = field.getAnnotation(Deserializable.class);
            return annotation != null ? annotation.value() : enumerated.name();
        }
        return String.valueOf(object);
    }
}
