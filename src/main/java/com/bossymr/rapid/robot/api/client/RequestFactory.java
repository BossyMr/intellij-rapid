package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.MastershipException;
import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.client.proxy.EntityProxy;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipStatus;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class RequestFactory {

    private final @NotNull NetworkManager manager;

    public RequestFactory(@NotNull NetworkManager manager) {
        this.manager = manager;
    }

    public @Nullable Object createQuery(@NotNull Class<?> type, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws Throwable {
        Class<?> returnType = method.getReturnType();
        if (returnType.isAnnotationPresent(Service.class)) {
            return manager.createService((returnType));
        }
        Service service = type.getAnnotation(Service.class);
        String path = service != null ? service.value() : "";
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof Fetch request) {
                if (method.getReturnType().isAssignableFrom(NetworkQuery.class)) {
                    return createNetworkCall(request.method(), path + request.value(), request.arguments(), proxy, method, args);
                }
                throw new ProxyException("Method '" + method + "' is annotated as '@NetworkQuery' but returns '" + method.getReturnType() + "', it should return '" + NetworkQuery.class.getName() + "'");
            }
            if (annotation instanceof Subscribable request) {
                if (method.getReturnType().isAssignableFrom(SubscribableNetworkQuery.class)) {
                    return createSubscribableNetworkQuery(request.value(), proxy, method, args);
                }
                throw new ProxyException("Method '" + method + "' is annotated as '@Subscribable' but returns '" + method.getReturnType() + "', it should return '" + SubscribableNetworkQuery.class.getName() + "'");
            }
        }
        if (method.isDefault()) {
            return InvocationHandler.invokeDefault(proxy, method, args);
        }
        throw new ProxyException("Cannot handle method '" + method.getName() + "' in '" + type.getName() + "'");
    }

    private @NotNull NetworkQuery<?> createNetworkCall(@NotNull FetchMethod command, @NotNull String path, @NotNull String @NotNull [] arguments, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        MultiMap<String, String> collected = collect(method, args, annotation -> annotation instanceof Argument argument ? argument.value() : null);
        for (String argument : arguments) {
            String key = argument.split("=")[0];
            String value;
            if (argument.contains("=")) {
                value = argument.substring(key.length() + 1);
            } else {
                value = null;
            }
            collected.put(key, value);
        }
        Type returnType = ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        NetworkRequest<?> request = new NetworkRequest<>(command, URI.create(interpolate(path, proxy, method, args)), GenericType.of(returnType));
        request.putArguments(collected);
        request.getFields().putAll(collect(method, args, annotation -> annotation instanceof Field field ? field.value() : null));
        NetworkQuery<?> query = manager.createQuery(request);
        if(!method.isAnnotationPresent(RequiresMastership.class)) {
            return query;
        }
        /*
         * If the method is annotated as @RequiresMastership, mastership must be retrieved prior to the actual request
         * being called. In addition, mastership must be released after the request has been called, even if the request
         * fails.
         */
        MastershipType mastershipType = method.getAnnotation(RequiresMastership.class).value();
        return () -> {
            MastershipService mastershipService = manager.createService(MastershipService.class);
            MastershipDomain mastershipDomain = mastershipService.getDomain(mastershipType).get();
            Boolean isHolding = mastershipDomain.isHolding();
            if(isHolding != null && isHolding) {
                /*
                 * Mastership is already being held. Mastership should not be released after the request is called,
                 * because some other code - the code that requested mastership - might still require mastership.
                 */
                return query.get();
            }
            MastershipStatus status = mastershipDomain.getStatus();
            if(status != MastershipStatus.NO_MASTER) {
                /*
                 * Mastership is not held by this client, but it is currently being held by some other client. As such,
                 * the request cannot be completed.
                 */
                throw new MastershipException(mastershipType, mastershipDomain.getApplication());
            }
            mastershipDomain.request().get();
            /*
             * Tell the network client that, if it closes before mastership is released, it needs to release mastership.
             * The network client might close if the request fails and the network client is configured to close on
             * failure.
             */
            manager.subscribe(() -> {
                /*
                 * Check that mastership is still being held.
                 */
                MastershipDomain domain = mastershipService.getDomain(mastershipType).get();
                Boolean holding = domain.isHolding();
                if(holding != null && holding) {
                    domain.release().get();
                }
            });
            Object result = query.get();
            /*
             * Check that mastership is still being held.
             */
            MastershipDomain domain = mastershipService.getDomain(mastershipType).get();
            Boolean holding = domain.isHolding();
            if(holding != null && holding) {
                domain.release().get();
            }
            return result;
        };
    }

    private @NotNull SubscribableNetworkQuery<?> createSubscribableNetworkQuery(@NotNull String path, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        Class<?> returnType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        SubscribableEvent<?> event = new SubscribableEvent<>(URI.create(interpolate(path, proxy, method, args)), returnType);
        return manager.createSubscribableQuery(event);
    }

    private @NotNull String interpolate(@NotNull String path, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        MultiMap<String, String> map = collect(method, args, annotation -> annotation instanceof Path argument ? argument.value() : null);
        List<String> query = new ArrayList<>();
        String replaced = Pattern.compile("\\{([^}]*)}").matcher(path)
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
                                         if (link.getQuery() != null) {
                                             query.add(link.getQuery());
                                         }
                                         return link.getPath();
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
                                         return map.get(value);
                                     }
                                     throw new ProxyException("Method '" + method.getName() + "' of '" + method.getDeclaringClass().getName() + "' does not provide value for '" + value + "'");
                                 });
        if (query.isEmpty()) {
            return replaced;
        }
        URI processed = URI.create(replaced);
        if (processed.getQuery() != null) {
            query.add(processed.getQuery());
            try {
                replaced = new URI(processed.getScheme(), processed.getUserInfo(), processed.getHost(), processed.getPort(), processed.getPath(), null, null).toString();
            } catch (URISyntaxException e) {
                throw new ProxyException(e);
            }
        }
        String complete = String.join("&", query);
        return replaced + "?" + complete;
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
                            map.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                        }
                    } else {
                        throw new ProxyException("Parameter of '" + method.getName() + "' should be Map<String, String>");
                    }
                } else {
                    map.put(name, convert(args[i]));
                }
            }
        }
        return map;
    }

    private @NotNull String convert(@NotNull Object object) throws NoSuchFieldException {
        if (object instanceof Enum<?> enumerated) {
            var field = object.getClass().getField(enumerated.name());
            Deserializable annotation = field.getAnnotation(Deserializable.class);
            return annotation != null ? annotation.value()[0] : enumerated.name();
        }
        return String.valueOf(object);
    }
}
