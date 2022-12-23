package com.bossymr.rapid.robot.network.client.impl;

import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.query.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AsynchronousQueryImpl implements AsynchronousQuery {

    private final NetworkClient networkClient;
    private final HttpRequest request;

    public AsynchronousQueryImpl(@NotNull NetworkClient networkClient, @NotNull HttpRequest request) {
        this.networkClient = networkClient;
        this.request = request;
    }

    @Override
    public @NotNull AsynchronousEntity send() throws IOException, InterruptedException {
        HttpResponse<byte[]> response = networkClient.send(request);
        String location = response.headers().firstValue("Location").orElseThrow();
        return new AsynchronousEntityImpl(networkClient, URI.create(location));
    }

    @Override
    public @NotNull CompletableFuture<AsynchronousEntity> sendAsync() {
        return networkClient.sendAsync(request)
                .thenApplyAsync(response -> {
                    String location = response.headers().firstValue("Location").orElseThrow();
                    return new AsynchronousEntityImpl(networkClient, URI.create(location));
                });
    }

    public static class AsynchronousEntityImpl implements AsynchronousEntity {

        private final NetworkClient networkClient;
        private final URI path;

        public AsynchronousEntityImpl(@NotNull NetworkClient networkClient, @NotNull URI path) {
            this.networkClient = networkClient;
            this.path = path;
        }

        @Override
        public @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscriptionEntity, AsynchronousEvent> onEvent) throws IOException, InterruptedException {
            SubscriptionEntity subscriptionEntity = new SubscribableQueryImpl.SubscriptionEntityImpl(networkClient);
            networkClient.subscribe(subscriptionEntity, path.getPath(), priority, model -> onEvent.accept(subscriptionEntity, model), AsynchronousEvent.class);
            return subscriptionEntity;
        }

        @Override
        public @NotNull Query<AsynchronousEvent> poll() {
            HttpRequest request = HttpRequest.newBuilder(path).build();
            return networkClient.newQuery(request, AsynchronousEvent.class);
        }
    }
}
