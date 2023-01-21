package com.bossymr.network;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * A {@code NetworkAction} represents a certain policy for handling either successful or unsuccessful responses.
 * <h2>Usage</h2>
 * A {@code NetworkAction} defines the methods, {@link #send(NetworkCall)} and @link #sendAsync(NetworkCall)} to send
 * requests both synchronously and asynchronously, {@link #join()} to wait for all asynchronous requests to complete,
 * and {@link #shutdown()} to close this {@code NetworkAction} and cancel all remaining incomplete requests.
 * <h2>Extending NetworkAction</h2>
 * {@code NetworkAction} can be extended, in which case {@link #onSuccess(NetworkCall, T)} or
 * {@link #onFailure(NetworkCall, Throwable)} should be overridden to implement a certain policy. For example,
 * {@code onFailure} can be overridden to log exceptions, or to call {@link #shutdown()} to cancel all requests if a
 * request fails.
 */
public class NetworkAction<T> {

    private final Set<CompletableFuture<? extends T>> ongoing = ConcurrentHashMap.newKeySet();
    private boolean closed;

    private boolean isClosed() {
        return closed;
    }

    /**
     * Sends the specified {@link NetworkCall} synchronously.
     *
     * @param networkCall the request.
     * @param <U> the type of response body.
     * @return the response, or {@code null} if a response body was not received.
     * @throws IllegalStateException if this {@code NetworkAction} is closed.
     * @throws IOException if an I/O error has occurred.
     * @throws InterruptedException if this {@code NetworkAction} is interrupted.
     */
    public <U extends T> @Nullable U send(@NotNull NetworkCall<? extends U> networkCall) throws IllegalStateException, IOException, InterruptedException {
        if (isClosed()) {
            throw new IllegalStateException("Failed to send '" + networkCall + "' synchronously as NetworkAction is closed");
        }
        try {
            U response = networkCall.send();
            onSuccess(networkCall, response);
            return response;
        } catch (IOException | RuntimeException e) {
            onFailure(networkCall, e);
            throw e;
        }
    }

    /**
     * Sends the specified {@link NetworkCall} asynchronously. If this {@code NetworkAction} is already closed, the
     * returned response will be canceled.
     *
     * @param networkCall the request.
     * @param <U> the type of response body.
     * @return the asynchronous response.
     */
    public <U extends T> @NotNull CompletableFuture<U> sendAsync(@NotNull NetworkCall<? extends U> networkCall) {
        if (isClosed()) {
            CompletableFuture<U> request = new CompletableFuture<>();
            request.cancel(false);
            return request;
        }
        CompletableFuture<? extends U> request = networkCall.sendAsync();
        ongoing.add(request);
        return request.handleAsync((response, throwable) -> {
            ongoing.remove(request);
            if (throwable != null) {
                onFailure(networkCall, throwable);
                throw throwable instanceof CompletionException ? ((CompletionException) throwable) : new CompletionException(throwable);
            }
            onSuccess(networkCall, response);
            return response;
        });
    }

    /**
     * This method is invoked for each successful response.
     * <p>
     * This method can be overridden to specify a specific policy to handle successful responses.
     *
     * @param networkCall the network call.
     * @param response the response body, or {@code null} if a response body was not provided.
     */
    protected <U extends T> void onSuccess(@NotNull NetworkCall<? extends U> networkCall, @Nullable U response) {}

    /**
     * This method is invoked for each unsuccessful response.
     * <p>
     * This method can be overridden to specify a specific policy to handle unsuccessful responses.
     *
     * @param networkCall the network call.
     * @param throwable the throwable,
     */
    protected <U extends T> void onFailure(@NotNull NetworkCall<? extends U> networkCall, @NotNull Throwable throwable) {}

    /**
     * Waits for all outstanding requests to complete or until this {@code NetworkAction} is shutdown.
     *
     * @throws InterruptedException if this {@code NetworkAction} is interrupted.
     * @throws IOException if an I/O error has occurred.
     */
    public void join() throws IOException, InterruptedException {
        if (isClosed()) return;
        try {
            CompletableFuture.allOf(ongoing.toArray(CompletableFuture[]::new)).get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException checkedException) throw checkedException;
            if (cause instanceof RuntimeException uncheckedException) throw uncheckedException;
            // A request should not be able to throw a checked exception.
            throw new IllegalStateException(cause);
        }
    }

    /**
     * Immediately closes this {@code NetworkAction} and cancels all ongoing requests.
     */
    public void shutdown() {
        if (isClosed()) return;
        closed = true;
        for (CompletableFuture<? extends T> request : ongoing) {
            request.cancel(false);
        }
    }
}

