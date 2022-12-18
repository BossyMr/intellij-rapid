package com.bossymr.rapid.robot.network.query;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
     * @throws InterruptedException if the operation is interrupted.
     */
    T send() throws IOException, InterruptedException;

    /**
     * Sends the request to the server and returns the response asynchronously.
     *
     * @return the asynchronous response.
     */
    @NotNull CompletableFuture<T> sendAsync();

}
