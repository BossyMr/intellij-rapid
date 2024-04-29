package com.bossymr.network.client;

import java.io.IOException;

/**
 * A {@code NetworkQuery} represents a query to a remote resource.
 *
 * @param <T> the value of the remote resource.
 */
public interface NetworkQuery<T> {

    /**
     * Executes this query and converts the response into an object of the specified type.
     *
     * @return the response.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if the current thread is interrupted.
     */
    T get() throws IOException, InterruptedException;

}
