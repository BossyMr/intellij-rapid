package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public interface SubscribableQuery<T> {

    @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscriptionEntity, T> onEvent);


}
