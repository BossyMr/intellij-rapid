package com.bossymr.rapid.robot.api;

import com.bossymr.rapid.robot.function.ThrowableConsumer;
import com.bossymr.rapid.robot.function.ThrowableFunction;

import java.io.IOException;
import java.net.URI;

/**
 * A {@code NetworkQuery} represents a network query.
 *
 * @param <T> the return type of the query.
 */
public interface NetworkQuery<T> {

    /**
     * {@return the return type of this query}
     */
    GenericType<T> getType();

    /**
     * {@return the path of this query}
     */
    URI getPath();

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
     * @param type the type of the new query.
     * @param mapper a function to apply to the result of this query.
     * @param <E> the type of the new query.
     * @return a new query.
     */
    default <E> NetworkQuery<E> map(GenericType<E> type, ThrowableFunction<? super T, ? extends E> mapper) {
        return new NetworkQuery<>() {
            @Override
            public GenericType<E> getType() {
                return type;
            }

            @Override
            public URI getPath() {
                return NetworkQuery.this.getPath();
            }

            @Override
            public E get() throws IOException, InterruptedException {
                T value = NetworkQuery.this.get();
                return mapper.apply(value);
            }
        };
    }

    /**
     * Returns a {@code NetworkQuery} that will pass the result of this query to the given function before returning
     * it.
     *
     * @param consumer a function that will be called with the result of this query.
     * @return a new query.
     */
    default NetworkQuery<T> peek(ThrowableConsumer<? super T> consumer) {
        return new NetworkQuery<>() {
            @Override
            public GenericType<T> getType() {
                return NetworkQuery.this.getType();
            }

            @Override
            public URI getPath() {
                return NetworkQuery.this.getPath();
            }

            @Override
            public T get() throws IOException, InterruptedException {
                T value = NetworkQuery.this.get();
                consumer.accept(value);
                return value;
            }
        };
    }

    /**
     * Returns a new {@code NetworkQuery} that will catch any exception and pass it to the given function before
     * rethrowing it.
     *
     * @param consumer a function that will be called with any caught exception.
     * @return a new query.
     */
    default NetworkQuery<T> exceptionally(ThrowableConsumer<? super Throwable> consumer) {
        return new NetworkQuery<T>() {
            @Override
            public GenericType<T> getType() {
                return NetworkQuery.this.getType();
            }

            @Override
            public URI getPath() {
                return NetworkQuery.this.getPath();
            }

            @Override
            public T get() throws IOException, InterruptedException {
                try {
                    return NetworkQuery.this.get();
                } catch (Exception e) {
                    try {
                        consumer.accept(e);
                    } catch (Exception ex) {
                        e.addSuppressed(ex);
                    }
                    throw e;
                }
            }
        };
    }
}
