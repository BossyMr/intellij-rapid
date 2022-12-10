package com.bossymr.rapid.robot.network.query;

import com.bossymr.rapid.robot.network.EntityModel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.annotation.*;
import java.util.function.BiConsumer;

public interface SubscribableQuery<T extends EntityModel> {

    @NotNull SubscriptionEntity subscribe(@NotNull SubscriptionPriority priority, @NotNull BiConsumer<SubscriptionEntity, T> onEvent) throws IOException;


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
