package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.entity.ResponseModel;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Semaphore;

public class SubscriptionGroup {
    private static final Logger logger = Logger.getInstance(SubscriptionGroup.class);

    private final NetworkClient networkClient;
    private final HttpClient httpClient;

    private final List<SubscriptionEntity> entities;
    private final Semaphore semaphore = new Semaphore(1);

    private @Nullable WebSocket webSocket;
    private @Nullable URI path;

    public SubscriptionGroup(@NotNull NetworkClient networkClient, @NotNull HttpClient httpClient) {
        this.networkClient = networkClient;
        this.httpClient = httpClient;
        this.entities = new ArrayList<>();
    }

    private static void onEntity(@NotNull List<SubscriptionEntity> entities, @NotNull EntityModel model) {
        logger.debug("Received event '" + model + "'");
        for (SubscriptionEntity entity : List.copyOf(entities)) {
            String path = Objects.requireNonNull(model.getLink("self")).getPath();
            String event = entity.getEvent().getPath().toString();
            if (path.startsWith(event)) {
                logger.debug("Sending event '" + model + "' to entity '" + entity + "'");
                entity.event(model);
            }
        }
    }

    private static @NotNull @Unmodifiable List<SubscriptionEntity> getUnique(@NotNull List<SubscriptionEntity> entities) {
        Map<URI, SubscriptionEntity> cache = new HashMap<>();
        for (SubscriptionEntity entity : entities) {
            URI resource = entity.getEvent().getPath();
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

    private static @NotNull MultiMap<String, String> getBody(@NotNull List<SubscriptionEntity> entities) {
        MultiMap<String, String> map = new MultiMap<>();
        List<SubscriptionEntity> unique = getUnique(entities);
        for (int i = 0; i < unique.size(); i++) {
            SubscriptionEntity entity = unique.get(i);
            map.put("resources", String.valueOf(i));
            map.put(String.valueOf(i), String.valueOf(entity.getEvent().getPath()));
            map.put(i + "-p", String.valueOf(entity.getPriority().ordinal()));
        }
        return map;
    }

    public void update() throws InterruptedException, IOException {
        semaphore.acquire();
        try {
            if (getEntities().isEmpty()) {
                close();
            } else if (path == null || webSocket == null) {
                start();
            } else {
                logger.debug("Updating SubscriptionGroup '{}'", getEntities());
                networkClient.send(NetworkTarget.newTarget(RequestMethod.PUT, path, GenericType.voidType())
                        .properties(getBody(getEntities()))
                        .build());
            }
        } finally {
            semaphore.release();
        }
    }

    private void start() throws IOException, InterruptedException {
        logger.debug("Starting SubscriptionGroup '{}'", getEntities());
        HttpResponse<byte[]> response = networkClient.send(NetworkTarget.newTarget(RequestMethod.POST, URI.create("/subscription"), GenericType.voidType())
                .properties(getBody(getEntities()))
                .build());
        ResponseModel model = ResponseModel.fromXML(new String(response.body(), StandardCharsets.UTF_8));
        String path = response.headers().firstValue("Location").orElseThrow();
        httpClient.newWebSocketBuilder()
                .subprotocols("robapi2_subscription")
                .buildAsync(URI.create(path), new java.net.http.WebSocket.Listener() {
                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        ResponseModel model = ResponseModel.fromXML(data.toString());
                        for (EntityModel entity : model.getEntities()) {
                            onEntity(entities, entity);
                        }
                        return CompletableFuture.completedFuture(null);
                    }

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        logger.debug("Started WebSocket");
                    }

                    @Override
                    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                        logger.debug("Closed WebSocket: " + reason);
                        return CompletableFuture.completedFuture(null);
                    }
                }).thenAccept(webSocket -> this.webSocket = webSocket);
        this.path = model.getLink("group");
    }

    private void close() throws IOException, InterruptedException {
        logger.debug("Closing SubscriptionGroup");
        if (path == null || webSocket == null) {
            return;
        }
        NetworkTarget<Void> target = NetworkTarget.newTarget(RequestMethod.DELETE, path, GenericType.voidType()).build();
        path = null;
        try {
            networkClient.send(target);
            webSocket.sendClose(1000, "");
        } finally {
            webSocket = null;
        }
    }

    public @NotNull List<SubscriptionEntity> getEntities() {
        return entities;
    }
}
