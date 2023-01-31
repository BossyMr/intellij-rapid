package com.bossymr.network.client;

import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.model.CollectionModel;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Semaphore;

public class SubscriptionGroup {

    private final @NotNull NetworkClient networkClient;
    private final @NotNull Set<SubscriptionEntity> entities;
    private final Semaphore semaphore = new Semaphore(1);

    private @Nullable URI path;
    private @Nullable WebSocket webSocket;

    private SubscriptionGroup(@NotNull NetworkClient networkClient, @NotNull URI path, @NotNull WebSocket webSocket, @NotNull Set<SubscriptionEntity> entities) {
        this.networkClient = networkClient;
        this.path = path;
        this.webSocket = webSocket;
        this.entities = entities;
    }

    public static @NotNull CompletableFuture<SubscriptionGroup> start(@NotNull HttpNetworkClient networkClient, @NotNull Set<SubscriptionEntity> entities, @NotNull URI defaultPath) {
        HttpRequest request = HttpRequest.newBuilder(defaultPath.resolve("/subscription"))
                .POST(HttpRequest.BodyPublishers.ofString(getBody(entities)))
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        return networkClient.sendAsync(request)
                .thenComposeAsync(response -> {
                    CollectionModel collectionModel = CollectionModel.convert(response.body());
                    collectionModel.getModels().forEach(model -> onEntity(entities, model));
                    URI path = URI.create(response.headers().firstValue("Location").orElseThrow());
                    return networkClient.getHttpClient().newWebSocketBuilder()
                            .subprotocols("robapi2_subscription")
                            .buildAsync(path, new SubscriptionListener(entities))
                            .thenApplyAsync(webSocket -> new SubscriptionGroup(networkClient, collectionModel.getLink("group"), webSocket, entities));
                });
    }

    private static void onEntity(@NotNull Set<SubscriptionEntity> entities, @NotNull Model model) {
        for (SubscriptionEntity entity : entities) {
            String path = model.getLink("self").getPath();
            String event = entity.getEvent().getResource().toString();
            if (path.startsWith(event)) {
                entity.onEvent(model);
            }
        }
    }

    private static @NotNull @Unmodifiable List<SubscriptionEntity> getUnique(@NotNull Set<SubscriptionEntity> entities) {
        Map<URI, SubscriptionEntity> cache = new HashMap<>();
        for (SubscriptionEntity entity : entities) {
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

    private static @NotNull String getBody(@NotNull Set<SubscriptionEntity> entities) {
        StringJoiner result = new StringJoiner("&");
        List<SubscriptionEntity> unique = getUnique(entities);
        for (int i = 0; i < unique.size(); i++) {
            SubscriptionEntity entity = unique.get(i);
            result.add("resources=" + i);
            result.add(i + "=" + entity.getEvent().getResource());
            result.add(i + "-p=" + entity.getPriority().ordinal());
        }
        return result.toString();
    }

    private @NotNull CompletableFuture<Void> acquireAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        });
    }

    public @NotNull CompletableFuture<Void> update() {
        return acquireAsync()
                .thenComposeAsync(ignored -> {
                    if (path == null || webSocket == null) {
                        throw new IllegalArgumentException("Subscription group '" + path + "' is closed");
                    }
                    HttpRequest request = HttpRequest.newBuilder(path)
                            .PUT(HttpRequest.BodyPublishers.ofString(getBody(getEntities())))
                            .setHeader("Content-Type", "application/x-www-form-urlencoded")
                            .build();
                    return networkClient.sendAsync(request);
                }).handleAsync((response, throwable) -> {
                    semaphore.release();
                    return null;
                });
    }

    public @NotNull CompletableFuture<Void> close() {
        return acquireAsync()
                .thenComposeAsync(ignored -> {
                    if (path == null || webSocket == null) {
                        throw new IllegalArgumentException("Subscription group '" + path + "' is closed");
                    }
                    HttpRequest request = HttpRequest.newBuilder(path)
                            .DELETE()
                            .build();
                    this.path = null;
                    return networkClient.sendAsync(request);
                }).thenComposeAsync(response -> {
                    if (path == null || webSocket == null) {
                        throw new IllegalArgumentException("Subscription group '" + path + "' is closed");
                    }
                    return webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "");
                }).handleAsync((webSocket, throwable) -> {
                    semaphore.release();
                    this.webSocket = null;
                    return null;
                });
    }

    public @NotNull Set<SubscriptionEntity> getEntities() {
        return entities;
    }

    private static class SubscriptionListener implements WebSocket.Listener {

        private final @NotNull Set<SubscriptionEntity> entities;
        private @NotNull StringBuilder stringBuilder = new StringBuilder();

        public SubscriptionListener(@NotNull Set<SubscriptionEntity> entities) {
            this.entities = entities;
        }

        @Override
        public @Nullable CompletionStage<?> onText(@NotNull WebSocket webSocket, @NotNull CharSequence data, boolean last) {
            stringBuilder.append(data);
            if (last) {
                String event = stringBuilder.toString();
                CollectionModel collectionModel = CollectionModel.convert(event.getBytes());
                for (Model model : collectionModel.getModels()) {
                    onEntity(entities, model);
                }
                stringBuilder = new StringBuilder();
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }
}
