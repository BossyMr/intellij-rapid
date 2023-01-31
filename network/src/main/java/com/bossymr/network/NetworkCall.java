package com.bossymr.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code NetworkCall} represents a callable network request.
 *
 * @param <T> the type of response body.
 */
public interface NetworkCall<T> extends AutoCloseable {

    /**
     * Sends the request synchronously.
     *
     * @return the response, or {@code null} if a response body was not received.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if this {@code NetworkCall} is interrupted.
     * @throws IllegalArgumentException if this {@code NetworkCall} is closed.
     */
    @Nullable T send() throws IOException, InterruptedException;

    /**
     * Sends the request asynchronously.
     *
     * @return the asynchronous response.
     * @throws IllegalArgumentException if this {@code NetworkCall} is closed.
     */
    @NotNull CompletableFuture<T> sendAsync();

    /**
     * Closes this {@code NetworkCall} and cancels all ongoing asynchronous requests.
     */
    @Override
    void close();
}
