package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public interface AsynchronousEntity<T> {

    @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscriptionEntity, T> onEvent);

    @NotNull T send() throws IOException, InterruptedException;

    @NotNull CompletableFuture<T> sendAsync();

}
