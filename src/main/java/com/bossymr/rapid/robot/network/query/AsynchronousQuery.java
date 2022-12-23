package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

public interface AsynchronousQuery extends Query<AsynchronousEntity> {

    /**
     * Indicates that this endpoint is asynchronous.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Asynchronous {

        /**
         * Specifies the path of the request.
         *
         * @return the path of the request.
         */
        @NotNull String value();
    }

}
