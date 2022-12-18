package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.*;
import java.util.function.BiConsumer;

public interface SubscribableQuery<T> {

    @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscriptionEntity, T> onEvent) throws IOException, InterruptedException;


    /**
     * Indicates that this endpoint is subscribable.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Subscribable {

        /**
         * Specifies the resource.
         *
         * @return the resource.
         */
        @NotNull String value();
    }

}
