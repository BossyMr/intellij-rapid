package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.Link;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.client.impl.QueryImpl;
import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.client.security.Authenticator;
import com.bossymr.rapid.robot.network.client.security.impl.DigestAuthenticator;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscriptionEntity;
import com.bossymr.rapid.robot.network.query.SubscriptionPriority;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class NetworkClient {

    private static final Logger LOG = Logger.getInstance(NetworkClient.class);

    private static final CookieManager COOKIE_MANAGER = new CookieManager();
    private final Authenticator authenticator;

    private final URI path;
    private final HttpClient httpClient;
    private final Map<SubscriptionEntity, SubscriptionDetail<?>> subscriptions = new HashMap<>();
    private URI subscriptionGroup;
    private WebSocket webSocket;

    public NetworkClient(@NotNull URI path, @NotNull Credentials credentials) {
        this.path = path;
        this.authenticator = new DigestAuthenticator(() -> credentials);
        COOKIE_MANAGER.getCookieStore().removeAll();
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(COOKIE_MANAGER)
                .build();
    }

    @SuppressWarnings("unchecked")
    public <T> @NotNull T newService(@NotNull Class<T> service) {
        return (T) Proxy.newProxyInstance(
                service.getClassLoader(),
                new Class[]{service},
                new ServiceInvocationHandler(service, this));
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityModel> @NotNull T newEntity(@NotNull Class<T> entity, @NotNull Model model) {
        return (T) Proxy.newProxyInstance(
                entity.getClassLoader(),
                new Class[]{entity},
                new EntityInvocationHandler(entity, this, model));
    }

    public @NotNull URI getPath() {
        return path;
    }

    public @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) throws IOException {
        HttpResponse<byte[]> response = retry(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 300) {
            throw ResponseStatusException.of(response);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> retry(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException {
        HttpRequest authenticated = authenticator.authenticate(request);
        HttpResponse<T> response = notify(authenticated != null ? authenticated : request, bodyHandler);
        if (response.statusCode() == 401 || response.statusCode() == 407) {
            HttpRequest retry = authenticator.authenticate(response);
            if (retry != null) {
                return notify(retry, bodyHandler);
            }
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> notify(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException {
        HttpResponse<T> response;
        try {
            response = httpClient.send(request, bodyHandler);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        LOG.info("Request: '" + request + "' with response: '" + response + "'");
        return response;
    }

    public @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest request) {
        return retryAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(response -> {
                    if (response.statusCode() >= 300) {
                        try {
                            throw ResponseStatusException.of(response);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }
                    return response;
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> retryAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest authenticated = authenticator.authenticate(request);
        return httpClient.sendAsync(authenticated != null ? authenticated : request, bodyHandler)
                .thenComposeAsync(response -> {
                    if (response.statusCode() == 401 || response.statusCode() == 407) {
                        HttpRequest retry = authenticator.authenticate(response);
                        if (retry != null) {
                            return notifyAsync(retry, bodyHandler);
                        }
                    }
                    return CompletableFuture.completedFuture(response);
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> notifyAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        return httpClient.sendAsync(request, bodyHandler)
                .thenApplyAsync(response -> {
                    LOG.info("Request: '" + request + "' with response: '" + response + "'");
                    return response;
                });
    }

    public <T> @Nullable T process(@NotNull HttpResponse<byte[]> response, @NotNull Type returnType) throws IOException {
        byte[] body = response.body();
        if (body.length == 0) return null;
        CollectionModel collectionModel = CollectionModel.getModel(body);
        List<Model> models = new ArrayList<>(collectionModel.models());
        Link link;
        while ((link = collectionModel.model().getLink("next")) != null) {
            HttpRequest request = HttpRequest.newBuilder(response.request(), (n, v) -> true)
                    .uri(link.path())
                    .build();
            HttpResponse<byte[]> next = send(request);
            collectionModel = CollectionModel.getModel(next.body());
            models.addAll(collectionModel.models());
        }
        return process(models, returnType);
    }

    public <T> @NotNull CompletableFuture<T> processAsync(@NotNull HttpResponse<byte[]> response, @NotNull Type returnType) throws IOException {
        return processAsync(new ArrayList<>(), response, returnType);
    }

    public <T> @NotNull CompletableFuture<T> processAsync(@NotNull List<Model> models, @NotNull HttpResponse<byte[]> response, @NotNull Type returnType) throws IOException {
        byte[] body = response.body();
        if (body.length == 0) return CompletableFuture.completedFuture(null);
        CollectionModel collectionModel = CollectionModel.getModel(body);
        models.addAll(collectionModel.models());
        Link next = collectionModel.model().getLink("next");
        if (next != null) {
            HttpRequest request = HttpRequest.newBuilder(response.request(), (n, v) -> true)
                    .uri(next.path())
                    .build();
            return sendAsync(request)
                    .thenComposeAsync(httpResponse -> {
                        try {
                            return processAsync(models, httpResponse, returnType);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    });
        } else {
            return processAsync(models, returnType);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T process(@NotNull List<Model> models, @NotNull Type returnType) {
        if (returnType == Void.class) return null;
        Class<? extends EntityModel> type = getReturnType(returnType);
        List<? extends EntityModel> entities = getEntities(models, type);
        if (returnType instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType().equals(List.class)) {
                return (T) Collections.unmodifiableList(entities);
            }
            if (parameterizedType.getRawType().equals(Set.class)) {
                return (T) Set.copyOf(entities);
            }
        } else {
            if (entities.size() == 1) {
                return (T) entities.get(0);
            }
        }
        LOG.error("Cannot convert '" + models + "' into '" + returnType + "'");
        return null;
    }

    public <T extends EntityModel> @NotNull List<T> getEntities(@NotNull List<Model> models, @NotNull Class<T> type) {
        Entity annotation = type.getAnnotation(Entity.class);
        LOG.assertTrue(annotation != null, "Entity '" + type.getName() + "' not annotated");
        String[] names = annotation.value();
        List<T> entities = new ArrayList<>();
        for (Model model : models) {
            for (String name : names) {
                if (name.equals(model.type())) {
                    entities.add(process(model, type));
                }
            }
        }
        return entities;
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityModel> @NotNull Class<T> getReturnType(@NotNull Type returnType) {
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length == 1) {
                return getReturnType(typeArguments[0]);
            }
        }
        if (returnType instanceof Class<?> classType) {
            if (EntityModel.class.isAssignableFrom(classType)) {
                return (Class<T>) returnType;
            }
        }
        LOG.error("Invalid return type '" + returnType + "'");
        throw new AssertionError();
    }

    public <T extends EntityModel> @NotNull T process(@NotNull Model model, @NotNull Class<T> returnType) {
        return newEntity(returnType, model);
    }

    public <T> @NotNull CompletableFuture<T> processAsync(@NotNull List<Model> models, @NotNull Type returnType) {
        return CompletableFuture.completedFuture(process(models, returnType));
    }

    public <T extends EntityModel> void subscribe(@NotNull SubscriptionEntity subscriptionEntity, @NotNull String resource, @NotNull SubscriptionPriority subscriptionPriority, @NotNull Consumer<T> consumer, @NotNull Class<T> returnType) throws IOException {
        SubscriptionDetail<T> subscriptionDetail = new SubscriptionDetail<>(resource, subscriptionPriority, consumer, returnType);
        subscriptions.put(subscriptionEntity, subscriptionDetail);
        if (subscriptionGroup == null || webSocket == null) {
            // New subscription
            HttpRequest httpRequest = HttpRequest.newBuilder(path.resolve("/subscription"))
                    .POST(HttpRequest.BodyPublishers.ofString(getPayload()))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            HttpResponse<byte[]> response = send(httpRequest);
            CollectionModel model = CollectionModel.getModel(response.body());
            for (Model entity : model.models()) {
                consume(entity);
            }
            WebSocket.Listener listener = new WebSocket.Listener() {

                private StringBuilder stringBuilder = new StringBuilder();

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    stringBuilder.append(data);
                    if (last) {
                        try {
                            CollectionModel collectionModel = CollectionModel.getModel(stringBuilder.toString().getBytes());
                            for (Model entity : collectionModel.models()) {
                                consume(entity);
                            }
                        } catch (IOException e) {
                            LOG.error(e);
                        } finally {
                            stringBuilder = new StringBuilder();
                        }
                    }
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }
            };
            String location = response.headers().firstValue("Location").orElseThrow();
            URI path = URI.create(location);
            subscriptionGroup = Objects.requireNonNull(model.model().getLink("group")).path();
            webSocket = httpClient.newWebSocketBuilder()
                    .subprotocols("robapi2_subscription")
                    .buildAsync(path, listener)
                    .join();
        } else {
            // Update subscription
            HttpRequest httpRequest = HttpRequest.newBuilder(subscriptionGroup)
                    .PUT(HttpRequest.BodyPublishers.ofString(getPayload()))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            send(httpRequest);
        }
    }

    private void consume(@NotNull Model entity) {
        for (SubscriptionDetail<? extends EntityModel> detail : subscriptions.values()) {
            Entity annotation = detail.type().getAnnotation(Entity.class);
            assert annotation != null;
            for (String value : annotation.value()) {
                if (value.equals(entity.type())) {
                    process(entity, detail);
                }
            }
        }
    }

    public <T extends EntityModel> void process(@NotNull Model model, @NotNull SubscriptionDetail<T> detail) {
        Class<T> type = detail.type();
        T entity = process(model, type);
        detail.consumer().accept(entity);
    }

    public void unsubscribe(@NotNull SubscriptionEntity subscriptionEntity) throws IOException {
        SubscriptionDetail<?> subscriptionDetail = subscriptions.get(subscriptionEntity);
        if (subscriptionDetail == null) {
            throw new IllegalArgumentException();
        }
        subscriptions.remove(subscriptionEntity, subscriptionDetail);
        if (subscriptions.size() > 0) {
            // Edit subscription
            HttpRequest httpRequest = HttpRequest.newBuilder(subscriptionGroup)
                    .PUT(HttpRequest.BodyPublishers.ofString(getPayload()))
                    .setHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            send(httpRequest);
        } else {
            // Close subscription
            try {
                HttpRequest httpRequest = HttpRequest.newBuilder(subscriptionGroup).DELETE()
                        .build();
                send(httpRequest);
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                webSocket = null;
                subscriptionGroup = null;
            }
        }
    }

    public @NotNull String getPayload() {
        StringJoiner stringJoiner = new StringJoiner("&");
        List<SubscriptionDetail<?>> values = new ArrayList<>(subscriptions.values());
        Map<String, SubscriptionDetail<?>> subscriptions = new HashMap<>();
        for (SubscriptionDetail<?> value : values) {
            if (subscriptions.containsKey(value.path)) {
                SubscriptionDetail<?> cached = subscriptions.get(value.path);
                if (value.priority().ordinal() <= cached.priority().ordinal()) {
                    continue;
                }
            }
            subscriptions.put(value.path, value);
        }

        List<SubscriptionDetail<?>> details = new ArrayList<>(subscriptions.values());
        for (int i = 0; i < details.size(); i++) {
            SubscriptionDetail<?> subscriptionDetail = details.get(i);
            stringJoiner.add("resources=" + i);
            stringJoiner.add(i + "=" + subscriptionDetail.path);
            stringJoiner.add(i + "-p=" + subscriptionDetail.priority().ordinal());
        }
        return stringJoiner.toString();
    }

    public <T extends EntityModel> @NotNull Query<T> newQuery(@NotNull HttpRequest request, @NotNull Type responseType) {
        return new QueryImpl<>(this, request, responseType);
    }

    private record SubscriptionDetail<T extends EntityModel>(@NotNull String path,
                                                             @NotNull SubscriptionPriority priority,
                                                             @NotNull Consumer<T> consumer, @NotNull Class<T> type) {}

}
