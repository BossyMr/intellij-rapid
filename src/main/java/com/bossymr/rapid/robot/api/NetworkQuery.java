package com.bossymr.rapid.robot.api;

import java.io.IOException;

/**
 * A {@code NetworkQuery} represents a network query.
 *
 * @param <T> the return type of the query.
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
