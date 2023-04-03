package com.bossymr.network.client;

import com.bossymr.network.MultiMap;
import com.bossymr.network.SubscriptionEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Semaphore;

public class SubscriptionGroup {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionGroup.class);

    private final @NotNull NetworkClient networkClient;
    private final @NotNull Set<SubscriptionEntity> entities;
    private final @NotNull Semaphore semaphore = new Semaphore(1);

    private @Nullable WebSocket webSocket;
    private @Nullable URI path;

    public SubscriptionGroup(@NotNull NetworkClient networkClient) {
        this.networkClient = networkClient;
        this.entities = new HashSet<>();
    }

    private static void onEntity(@NotNull Set<SubscriptionEntity> entities, @NotNull EntityModel model) {
        logger.debug("Received event '" + model + "'");
        for (SubscriptionEntity entity : entities) {
            String path = Objects.requireNonNull(model.reference("self")).getPath();
            String event = entity.getEvent().getResource().toString();
            if (path.startsWith(event)) {
                logger.debug("Sending event '" + model + "' to entity '" + entity + "'");
                entity.event(model);
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

    public void update() throws InterruptedException, IOException {
        semaphore.acquire();
        try {
            if (getEntities().isEmpty()) {
                close();
            } else if (path == null || webSocket == null) {
                start();
            } else {
                logger.atDebug().log("Updating SubscriptionGroup '{}'", getEntities());
                HttpRequest request = networkClient.createRequest()
                        .setMethod("PUT")
                        .setPath(path)
                        .setFields(getBody(getEntities()))
                        .build();
                networkClient.send(request);
            }
        } finally {
            semaphore.release();
        }
    }

    private void start() throws IOException, InterruptedException {
        logger.atDebug().log("Starting SubscriptionGroup '{}'", getEntities());
        HttpRequest request = networkClient.createRequest()
                .setMethod("POST")
                .setPath(URI.create("/subscription"))
                .setFields(getBody(entities))
                .build();
        HttpResponse<byte[]> response = networkClient.send(request);
        ResponseModel model = ResponseModel.convert(response.body());
        URI path = URI.create(response.headers().firstValue("Location").orElseThrow());
        WebSocket webSocket = NetworkClient.computeAsync(networkClient.getHttpClient().newWebSocketBuilder()
                .subprotocols("robapi2_subscription")
                .buildAsync(path, new SubscriptionListener(entities)));
        this.path = model.model().reference("group");
        this.webSocket = webSocket;
    }

    private void close() throws IOException, InterruptedException {
        logger.atDebug().log("Closing SubscriptionGroup");
        if (path == null || webSocket == null) {
            return;
        }
        HttpRequest request = networkClient.createRequest()
                .setMethod("DELETE")
                .setPath(path)
                .build();
        this.path = null;
        try {
            networkClient.send(request);
            NetworkClient.computeAsync(webSocket.sendClose(WebSocket.NORMAL_CLOSURE, ""));
        } finally {
            webSocket = null;
        }
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
                ResponseModel model = ResponseModel.convert(event.getBytes());
                for (EntityModel entity : model.entities()) {
                    onEntity(entities, entity);
                }
                stringBuilder = new StringBuilder();
            }
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }
    }
}
