package com.bossymr.rapid.robot.network.client.impl;

import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscriptionEntity;
import com.bossymr.rapid.robot.network.query.SubscriptionPriority;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.function.BiConsumer;

public class SubscribableQueryImpl<T> implements SubscribableQuery<T> {

    private final NetworkClient networkClient;
    private final String resource;
    private final Class<T> returnType;

    public SubscribableQueryImpl(@NotNull NetworkClient networkClient, @NotNull String resource, @NotNull Class<T> returnType) {
        this.networkClient = networkClient;
        this.resource = resource;
        this.returnType = returnType;
    }

    @Override
    public @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscriptionEntity, T> onEvent) throws IOException, InterruptedException {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntityImpl(networkClient);
        networkClient.subscribe(subscriptionEntity, resource, priority, model -> onEvent.accept(subscriptionEntity, model), returnType);
        return subscriptionEntity;
    }

    public static class SubscriptionEntityImpl implements SubscriptionEntity {

        private final NetworkClient networkClient;

        public SubscriptionEntityImpl(NetworkClient networkClient) {
            this.networkClient = networkClient;
        }

        @Override
        public void unsubscribe() throws IOException, InterruptedException {
            networkClient.unsubscribe(this);
        }
    }
}
