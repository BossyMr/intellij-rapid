package com.bossymr.network.client;

import com.bossymr.network.MultiMap;
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
import java.util.concurrent.*;

public class SubscriptionGroup {

    private final @NotNull HttpNetworkClient networkClient;
    private final @NotNull Set<SubscriptionEntity> entities;
    private final @NotNull Semaphore semaphore = new Semaphore(1);
    private final @NotNull ExecutorService executorService;

    private @Nullable WebSocket webSocket;
    private @Nullable URI path;

    public SubscriptionGroup(@NotNull ExecutorService executorService, @NotNull HttpNetworkClient networkClient) {
        this.networkClient = networkClient;
        this.entities = new HashSet<>();
        this.executorService = executorService;
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

    private static @NotNull MultiMap<String, String> getBody(@NotNull Set<SubscriptionEntity> entities) {
        MultiMap<String, String> map = new MultiMap<>();
        List<SubscriptionEntity> unique = getUnique(entities);
        for (int i = 0; i < unique.size(); i++) {
            SubscriptionEntity entity = unique.get(i);
            map.add("resources", String.valueOf(i));
            map.add(String.valueOf(i), String.valueOf(entity.getEvent().getResource()));
            map.add(i + "-p", String.valueOf(entity.getPriority().ordinal()));
        }
        return map;
    }

    private @NotNull CompletableFuture<Void> acquireAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                throw new CompletionException(e);
            }
        }, executorService);
    }

    public @NotNull CompletableFuture<Void> update() {
        return acquireAsync()
                .thenComposeAsync(ignored -> {
                    if (getEntities().isEmpty()) {
                        return close();
                    }
                    if (path == null || webSocket == null) {
                        return start();
                    }
                    HttpRequest request = networkClient.createRequest()
                            .setMethod("PUT")
                            .setPath(path)
                            .setFields(getBody(getEntities()))
                            .build();
                    return networkClient.sendAsync(request)
                            .thenApplyAsync((response) -> null, executorService);
                }, executorService).handleAsync((response, throwable) -> {
                    semaphore.release();
                    return null;
                }, executorService);
    }

    private @NotNull CompletableFuture<Void> start() {
        HttpRequest request = networkClient.createRequest()
                .setMethod("POST")
                .setPath(URI.create("/subscription"))
                .setFields(getBody(entities))
                .build();
        return networkClient.sendAsync(request)
                .thenComposeAsync(response -> {
                    CollectionModel collectionModel = CollectionModel.convert(response.body());
                    collectionModel.getModels().forEach(model -> onEntity(entities, model));
                    URI path = URI.create(response.headers().firstValue("Location").orElseThrow());
                    return networkClient.getHttpClient().newWebSocketBuilder()
                            .subprotocols("robapi2_subscription")
                            .buildAsync(path, new SubscriptionListener(entities))
                            .thenAcceptAsync((webSocket) -> {
                                this.path = collectionModel.getLink("group");
                                this.webSocket = webSocket;
                            }, executorService);
                }, executorService);
    }

    private @NotNull CompletableFuture<Void> close() {
        if (path == null || webSocket == null) {
            return CompletableFuture.completedFuture(null);
        }
        HttpRequest request = networkClient.createRequest()
                .setMethod("DELETE")
                .setPath(path)
                .build();
        this.path = null;
        return networkClient.sendAsync(request)
                .thenComposeAsync(response -> {
                    if (path == null || webSocket == null) {
                        return null;
                    }
                    return webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "");
                }, executorService).handleAsync((webSocket, throwable) -> {
                    semaphore.release();
                    this.webSocket = null;
                    return null;
                }, executorService);
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
