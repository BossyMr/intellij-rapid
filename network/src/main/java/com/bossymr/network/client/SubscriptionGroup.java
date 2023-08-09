package com.bossymr.network.client;

import com.bossymr.network.GenericType;
import com.bossymr.network.MultiMap;
import com.bossymr.network.SubscriptionEntity;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Semaphore;

public class SubscriptionGroup {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionGroup.class);

    private final @NotNull NetworkClient networkClient;
    private final @NotNull List<SubscriptionEntity> entities;
    private final @NotNull Semaphore semaphore = new Semaphore(1);

    private @Nullable WebSocket webSocket;
    private @Nullable URI path;

    public SubscriptionGroup(@NotNull NetworkClient networkClient) {
        this.networkClient = networkClient;
        this.entities = new ArrayList<>();
    }

    private static void onEntity(@NotNull List<SubscriptionEntity> entities, @NotNull EntityModel model) {
        logger.debug("Received event '" + model + "'");
        for (SubscriptionEntity entity : List.copyOf(entities)) {
            String path = Objects.requireNonNull(model.reference("self")).getPath();
            String event = entity.getEvent().getResource().toString();
            if (path.startsWith(event)) {
                logger.debug("Sending event '" + model + "' to entity '" + entity + "'");
                entity.event(model);
            }
        }
    }

    private static @NotNull @Unmodifiable List<SubscriptionEntity> getUnique(@NotNull List<SubscriptionEntity> entities) {
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

    private static @NotNull MultiMap<String, String> getBody(@NotNull List<SubscriptionEntity> entities) {
        MultiMap<String, String> map = new MultiMap<>();
        List<SubscriptionEntity> unique = getUnique(entities);
        for (int i = 0; i < unique.size(); i++) {
            SubscriptionEntity entity = unique.get(i);
            map.put("resources", String.valueOf(i));
            map.put(String.valueOf(i), String.valueOf(entity.getEvent().getResource()));
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
                logger.atDebug().log("Updating SubscriptionGroup '{}'", getEntities());
                NetworkRequest<Void> request = new NetworkRequest<>(FetchMethod.PUT, path, GenericType.of(Void.class));
                request.getFields().putAll(getBody(getEntities()));
                networkClient.send(request).close();
            }
        } finally {
            semaphore.release();
        }
    }

    private void start() throws IOException, InterruptedException {
        logger.atDebug().log("Starting SubscriptionGroup '{}'", getEntities());
        NetworkRequest<Void> request = new NetworkRequest<>(FetchMethod.POST, URI.create("/subscription"), GenericType.of(Void.class));
        request.getFields().putAll(getBody(entities));
        ResponseModel model;
        try (Response response = networkClient.send(request)) {
            model = ResponseModel.convert(response.body().bytes());
            Request webSocketRequest = new Request.Builder()
                    .url(Objects.requireNonNull(response.header("Location")))
                    .header("Sec-WebSocket-Protocol", "robapi2_subscription")
                    .build();
            WebSocket webSocket = networkClient.getHttpClient().newWebSocket(webSocketRequest, new WebSocketListener() {
                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                    ResponseModel model = ResponseModel.convert(bytes.toByteArray());
                    for (EntityModel entity : model.entities()) {
                        onEntity(entities, entity);
                    }
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    ResponseModel model = ResponseModel.convert(text.getBytes());
                    for (EntityModel entity : model.entities()) {
                        onEntity(entities, entity);
                    }
                }

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    logger.atDebug().log("WebSocket started");
                }

                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    logger.atDebug().log("WebSocket closed");
                }

                @Override
                public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    logger.atDebug().log("WebSocket closing");
                }
            });
            this.path = model.model().reference("group");
            this.webSocket = webSocket;
        }
    }

    private void close() throws IOException, InterruptedException {
        logger.atDebug().log("Closing SubscriptionGroup");
        if (path == null || webSocket == null) {
            return;
        }
        NetworkRequest<Void> request = new NetworkRequest<>(FetchMethod.DELETE, path, GenericType.of(Void.class));
        this.path = null;
        try {
            networkClient.send(request).close();
            webSocket.close(1000, "");
        } finally {
            webSocket = null;
        }
    }

    public @NotNull List<SubscriptionEntity> getEntities() {
        return entities;
    }

}
