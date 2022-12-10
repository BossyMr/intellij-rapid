package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.annotation.*;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code Query} represents a request to a remote server.
 *
 * @param <T> the response type.
 */
public interface Query<T> {

    /**
     * Sends the request to the server and returns the response synchronously.
     *
     * @return the response.
     * @throws IOException if an I/O error occurs.
     */
    @Nullable T send() throws IOException;

    /**
     * Sends the request to the server and returns the response asynchronously.
     *
     * @return the asynchronous response.
     */
    @NotNull CompletableFuture<T> sendAsync();

    /**
     * Indicates that this value is included in the request body.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Field {

        /**
         * Specifies the name of the field.
         *
         * @return the name of the field.
         */
        @NotNull String value();
    }

    /**
     * Indicates that this value is included in the request path.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Argument {

        /**
         * Specifies the name of the field.
         *
         * @return the name of the field.
         */
        @NotNull String value();
    }


    /**
     * Indicates that this value is concatenated into the request path.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Path {

        /**
         * Specifies the name of the field.
         *
         * @return the name of the field.
         */
        @NotNull String value();
    }

    /**
     * Indicates that this query will send a request with the method {@code GET}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface GET {

        /**
         * Specifies the path of the request.
         *
         * @return the path of the request.
         */
        @NotNull String value();
    }

    /**
     * Indicates that this query will send a request with the method {@code POST}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface POST {

        /**
         * Specifies the path of the request.
         *
         * @return the path of the request.
         */
        @NotNull String value();
    }

    /**
     * Indicates that this query will send a request with the method {@code PUT}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface PUT {

        /**
         * Specifies the path of the request.
         *
         * @return the path of the request.
         */
        @NotNull String value();
    }

    /**
     * Indicates that this query will send a request with the method {@code DELETE}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface DELETE {

        /**
         * Specifies the path of the request.
         *
         * @return the path of the request.
         */
        @NotNull String value();
    }


}
