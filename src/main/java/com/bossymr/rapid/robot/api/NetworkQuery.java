package com.bossymr.rapid.robot.api;

import java.io.IOException;
import java.util.function.Function;

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

    /**
     * Returns a {@code NetworkQuery} that will map the result of this query with the given function.
     *
     * @param mapper a function to apply to the result of this query.
     * @param <E> the type of the new query.
     * @return a new query.
     */
    default <E> NetworkQuery<E> map(Function<? super T, ? extends E> mapper) {
        return () -> mapper.apply(get());
    }

}
