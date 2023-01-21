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
public interface NetworkCall<T> {

    /**
     * Sends the request synchronously.
     *
     * @return the response body, or {@code null} if the response did not contain a response body.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if this {@code NetworkCall} is interrupted.
     */
    @Nullable T send() throws IOException, InterruptedException;

    /**
     * Sends the request asynchronously.
     *
     * @return the asynchronous response.
     */
    @NotNull CompletableFuture<T> sendAsync();

}
