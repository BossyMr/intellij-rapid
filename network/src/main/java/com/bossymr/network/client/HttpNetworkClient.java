package com.bossymr.network.client;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.impl.model.CollectionModel;
import com.bossymr.network.client.impl.model.Model;
import com.bossymr.network.client.security.Authenticator;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.client.security.impl.DigestAuthenticator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
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
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

/**
 * A {@code HttpNetworkClient} is a simple implementation of a {@link NetworkClient} which delegates requests to a
 * {@link HttpClient}.
 */
public class HttpNetworkClient implements NetworkClient {

    public static final String WEBSOCKET_PROTOCOl = "robapi2_subscription";
    private static final String FORM_BODY_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final int MAX_CONNECTIONS = 2;
    private final Semaphore channel = new Semaphore(MAX_CONNECTIONS);
    private final Semaphore group = new Semaphore(1);

    private final HttpClient httpClient;
    private final URI defaultPath;

    private final Authenticator authenticator;

    private final Set<SubscriptionEntity> subscriptions = new HashSet<>();
    private @Nullable URI subscriptionGroup;
    private @Nullable WebSocket webSocket;

    public HttpNetworkClient(@NotNull URI defaultPath, @NotNull Supplier<Credentials> credentials) {
        this.defaultPath = defaultPath;
        this.authenticator = new DigestAuthenticator(credentials);
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
    }

    @Override
    public @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = retry(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 300) {
            throw new ResponseStatusException(response);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> retry(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        HttpRequest authenticated = authenticator.authenticate(request);
        HttpResponse<T> response = send(Objects.requireNonNullElse(authenticated, request), bodyHandler);
        if (response.statusCode() == 401 || response.statusCode() == 407) {
            HttpRequest retry = authenticator.authenticate(response);
            if (retry != null) {
                return send(retry, bodyHandler);
            }
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> send(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        channel.acquire();
        try {
            return httpClient.send(request, bodyHandler);
        } finally {
            channel.release();
        }
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest request) {
        return retryAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(response -> {
                    if (response.statusCode() >= 300) {
                        throw new CompletionException(new ResponseStatusException(response));
                    }
                    return response;
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> retryAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest authentication = authenticator.authenticate(request);
        return sendAsync(authentication != null ? authentication : request, bodyHandler)
                .thenComposeAsync(response -> {
                    if (response.statusCode() == 401 || response.statusCode() == 407) {
                        HttpRequest retry = authenticator.authenticate(response);
                        if (retry != null) {
                            return sendAsync(retry, bodyHandler);
                        }
                    }
                    return CompletableFuture.completedFuture(response);
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> sendAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        return CompletableFuture.completedFuture(null)
                .thenComposeAsync(ignored -> {
                    try {
                        channel.acquire();
                        return httpClient.sendAsync(request, bodyHandler);
                    } catch (InterruptedException e) {
                        throw new CompletionException(e);
                    }
                })
                .handleAsync((response, throwable) -> {
                    channel.release();
                    if (throwable != null) {
                        throw throwable instanceof CompletionException ?
                                ((CompletionException) throwable) :
                                new CompletionException(throwable);
                    }
                    return response;
                });
    }

    private @NotNull String toBody() {
        StringJoiner result = new StringJoiner("&");
        List<SubscriptionEntity> entities = getUniqueEntities();
        for (int i = 0; i < entities.size(); i++) {
            SubscriptionEntity entity = entities.get(i);
            result.add("resources=" + i);
            result.add(i + "=" + entity.getEvent().getResource());
            result.add(i + "-p=" + entity.getPriority().ordinal());
        }
        return result.toString();
    }

    private @NotNull @Unmodifiable List<SubscriptionEntity> getUniqueEntities() {
        Map<URI, SubscriptionEntity> cache = new HashMap<>();
        for (SubscriptionEntity entity : subscriptions) {
            URI resource = entity.getEvent().getResource();
            if (cache.containsKey(resource)) {
                SubscriptionEntity cached = cache.get(resource);
                if (entity.getPriority().ordinal() <= cached.getPriority().ordinal()) {
                    continue;
                }
            }
            cache.put(resource, entity);
        }
        return List.copyOf(cache.values());
    }

    @Override
    public <T> @NotNull SubscriptionEntity subscribe(@NotNull SubscribableEvent<T> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener) {
        SubscriptionEntity entity = new SubscriptionEntity(this, event, priority, listener);
        subscriptions.add(entity);
        if (subscriptions.size() > 1) {
            updateSubscription();
        } else {
            startSubscription();
        }
        return entity;
    }

    @Override
    public void unsubscribe(@NotNull SubscriptionEntity entity) {
        if (!(subscriptions.contains(entity))) throw new IllegalArgumentException();
        subscriptions.remove(entity);
        if (subscriptions.size() > 0) {
            updateSubscription();
        } else {
            closeSubscription();
        }
    }

    private void startSubscription() {
        if (webSocket == null && subscriptionGroup == null) {
            HttpRequest request = HttpRequest.newBuilder(defaultPath.resolve("/subscription"))
                    .POST(HttpRequest.BodyPublishers.ofString(toBody()))
                    .setHeader("Content-Type", FORM_BODY_CONTENT_TYPE)
                    .build();
            CompletableFuture.completedFuture(null)
                    .thenComposeAsync(ignored -> {
                        try {
                            group.acquire();
                            return sendAsync(request);
                        } catch (InterruptedException e) {
                            throw new CompletionException(e);
                        }
                    })
                    .thenComposeAsync(response -> {
                        CollectionModel collectionModel = CollectionModel.convert(response.body());
                        for (Model model : collectionModel.getModels()) {
                            handleEntity(model);
                        }
                        URI path = URI.create(response.headers().firstValue("Location").orElseThrow());
                        subscriptionGroup = collectionModel.getLink("group");
                        return httpClient.newWebSocketBuilder()
                                .subprotocols(WEBSOCKET_PROTOCOl)
                                .buildAsync(path, new WebSocketListener());
                    })
                    .thenRunAsync(group::release);
        }
    }

    private void closeSubscription() {
        if (webSocket != null && subscriptionGroup != null) {
            CompletableFuture.completedFuture(null)
                    .thenComposeAsync(ignored -> {
                        try {
                            group.acquire();
                            HttpRequest request = HttpRequest.newBuilder(subscriptionGroup).DELETE().build();
                            this.subscriptionGroup = null;
                            return sendAsync(request);
                        } catch (InterruptedException e) {
                            throw new CompletionException(e);
                        }
                    })
                    .thenComposeAsync((response) -> webSocket.sendClose(WebSocket.NORMAL_CLOSURE, ""))
                    .thenRunAsync(() -> {
                        this.webSocket = null;
                        group.release();
                    });
        }
    }

    private void updateSubscription() {
        if (webSocket != null && subscriptionGroup != null) {
            HttpRequest request = HttpRequest.newBuilder(subscriptionGroup)
                    .PUT(HttpRequest.BodyPublishers.ofString(toBody()))
                    .setHeader("Content-Type", FORM_BODY_CONTENT_TYPE)
                    .build();
            CompletableFuture.completedFuture(null)
                    .thenComposeAsync(ignored -> {
                        try {
                            group.acquire();
                            return sendAsync(request);
                        } catch (InterruptedException e) {
                            throw new CompletionException(e);
                        }
                    })
                    .thenRunAsync(group::release);
        }
    }

    public void handleEntity(@NotNull Model model) {
        for (SubscriptionEntity entity : subscriptions) {
            String path = model.getLink("self").getPath();
            if (path.startsWith(entity.getEvent().getResource().toString())) {
                entity.onEvent(model);
            }
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        subscriptions.clear();
        if (subscriptionGroup != null && webSocket != null) {
            closeSubscription();
        }
    }

    private class WebSocketListener implements WebSocket.Listener {

        private StringBuilder stringBuilder = new StringBuilder();

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            stringBuilder.append(data);
            if (last) {
                String event = stringBuilder.toString();
                CollectionModel collectionModel = CollectionModel.convert(event.getBytes());
                for (Model model : collectionModel.getModels()) {
                    handleEntity(model);
                }
                stringBuilder = new StringBuilder();
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }
}
