package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.MastershipException;
import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.annotations.Field;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.bossymr.rapid.robot.api.client.proxy.EntityProxy;
import com.bossymr.rapid.robot.api.client.proxy.ProxyException;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipDomain;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipService;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipStatus;
import com.bossymr.rapid.robot.network.robotware.mastership.MastershipType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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

    private @NotNull NetworkQuery<?> createNetworkCall(@NotNull RequestMethod command, @NotNull String path, @NotNull String @NotNull [] arguments, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
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
        NetworkTarget<?> target = NetworkTarget.newTarget(command, URI.create(interpolate(path, proxy, method, args)), getNetworkType(returnType))
                .arguments(collected)
                .properties(collect(method, args, annotation -> annotation instanceof Field field ? field.value() : null))
                .build();
        NetworkQuery<?> query = manager.createQuery(target);
        if (!method.isAnnotationPresent(RequiresMastership.class)) {
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
            if (isHolding != null && isHolding) {
                /*
                 * Mastership is already being held. Mastership should not be released after the request is called,
                 * because some other code - the code that requested mastership - might still require mastership.
                 */
                return query.get();
            }
            MastershipStatus status = mastershipDomain.getStatus();
            if (status != MastershipStatus.NO_MASTER) {
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
            manager.subscribe(new NetworkManager.Listener() {
                @Override
                public void onClose() throws IOException, InterruptedException {
                    /*
                     * Check that mastership is still being held.
                     */
                    MastershipDomain domain = mastershipService.getDomain(mastershipType).get();
                    Boolean holding = domain.isHolding();
                    if (holding != null && holding) {
                        domain.release().get();
                    }
                }
            });
            Object result = query.get();
            /*
             * Check that mastership is still being held.
             */
            MastershipDomain domain = mastershipService.getDomain(mastershipType).get();
            Boolean holding = domain.isHolding();
            if (holding != null && holding) {
                domain.release().get();
            }
            return result;
        };
    }

    private @NotNull NetworkType<?> getNetworkType(Type type) {
        if (getRawType(type).equals(List.class)) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type typeArgument = parameterizedType.getActualTypeArguments()[0];
            if (!(typeArgument instanceof Class<?> classType)) {
                throw new IllegalArgumentException("could not convert type '" + type + "'");
            }
            return NetworkType.listType(classType);
        }
        if (!(type instanceof Class<?> classType)) {
            throw new ProxyException("could not convert type '" + type + "'");
        }
        if (classType == String.class) {
            return NetworkType.stringType();
        }
        if (classType == ResponseModel.class) {
            return NetworkType.modelType();
        }
        if (classType == Void.class) {
            return NetworkType.voidType();
        }
        return NetworkType.entityType(classType);
    }

    /**
     * Calculates the outermost type. For example, for {@code List<String>}, the outermost type is {@code List}.
     * Likewise, for {@code String[]}, the outermost type is {@code String}.
     *
     * @param type the type.
     * @return the outermost type of the specified type.
     */
    private @NotNull Class<?> getRawType(@NotNull Type type) {
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


    private @NotNull SubscribableNetworkQuery<?> createSubscribableNetworkQuery(@NotNull String path, @NotNull Object proxy, @NotNull Method method, Object @NotNull [] args) throws NoSuchFieldException {
        Class<?> returnType = (Class<?>) ((ParameterizedType) method.getGenericReturnType()).getActualTypeArguments()[0];
        SubscribableTarget<?> event = new SubscribableTarget<>(URI.create(interpolate(path, proxy, method, args)), returnType);
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
            Alias annotation = field.getAnnotation(Alias.class);
            return annotation != null ? annotation.value()[0] : enumerated.name();
        }
        return String.valueOf(object);
    }
}
