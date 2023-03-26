package com.bossymr.network.client;

import com.bossymr.network.*;
import com.bossymr.network.client.security.Authenticator;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.client.security.impl.DigestAuthenticator;
import com.bossymr.network.model.Model;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * A {@code HttpNetworkKClient} is an implementation of a {@link NetworkClient} with authentication support, support for
 * enforcing a set amount of max connections.
 */
public class HttpNetworkClient implements NetworkClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpNetworkClient.class);

    private final int MAX_CONNECTIONS = 1;
    private final Semaphore semaphore = new Semaphore(MAX_CONNECTIONS);

    private final @NotNull Authenticator authenticator;
    private final @NotNull ExecutorService executorService;
    private final @NotNull URI defaultPath;

    private final @NotNull SubscriptionGroup subscriptionGroup;
    private final @NotNull HttpClient httpClient;

    /**
     * Creates a new {@code HttpNetworkClient} with the specified default path and credentials.
     *
     * @param defaultPath the path to connect to.
     * @param credentials the credentials to connect with.
     */
    public HttpNetworkClient(@NotNull URI defaultPath, @NotNull Supplier<Credentials> credentials) {
        this.defaultPath = defaultPath;
        this.authenticator = new DigestAuthenticator(credentials);
        this.executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
        this.subscriptionGroup = new SubscriptionGroup(executorService, this);
    }

    public static @NotNull RuntimeException getThrowable(@NotNull Throwable throwable) {
        if (throwable instanceof RuntimeException runtimeException) {
            return runtimeException;
        } else {
            return new CompletionException(throwable);
        }
    }

    public @NotNull RequestBuilder createRequest() {
        return new RequestBuilder(getDefaultPath());
    }

    @Override
    public @NotNull URI getDefaultPath() {
        return defaultPath;
    }

    public @NotNull HttpClient getHttpClient() {
        return httpClient;
    }

    @Override
    public @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = retry(request, HttpResponse.BodyHandlers.ofByteArray());
        logger.atDebug().log("Sending request '{}' synchronously with response '{}'", request, response);
        if (response.statusCode() >= 300) {
            throw new ResponseStatusException(response);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> retry(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        HttpRequest authenticated = authenticator.authenticate(request);
        HttpResponse<T> response = send(Objects.requireNonNullElse(authenticated, request), bodyHandler);
        if (response.statusCode() == 401 || response.statusCode() == 407) {
            HttpRequest retry = authenticator.authenticate(response);
            if (retry != null) {
                logger.atDebug().log("Re-authenticated request '{}' with authenticator '{}'", request, authenticator);
                return send(retry, bodyHandler);
            }
            logger.atDebug().log("Failed to re-authenticated request '{}' with authenticator '{}'", request, authenticator);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> send(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        semaphore.acquire();
        try {
            HttpRequest copy = HttpRequest.newBuilder(request, (key, value) -> true)
                    .setHeader("Connection", "close")
                    .build();
            HttpResponse<T> response = httpClient.send(copy, bodyHandler);
            if (response.statusCode() == 503) {
                logger.atDebug().log("Retrying request with response'" + response + "'");
                Thread.sleep(1000);
                return httpClient.send(copy, bodyHandler);
            }
            return response;
        } finally {
            semaphore.release();
        }
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest request) {
        return retryAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(response -> {
                    logger.atDebug().log("Sending request '{} synchronously with response '{}'", request, response);
                    if (response.statusCode() >= 300) {
                        throw new CompletionException(new ResponseStatusException(response));
                    }
                    return response;
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> retryAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest authentication = authenticator.authenticate(request);
        return sendAsync(authentication != null ? authentication : request, bodyHandler)
                .thenComposeAsync(response -> {
                    if (response.statusCode() == 401 || response.statusCode() == 407) {
                        HttpRequest retry = authenticator.authenticate(response);
                        if (retry != null) {
                            logger.atDebug().log("Re-authenticated request '{}' with authenticator '{}'", request, authenticator);
                            return sendAsync(retry, bodyHandler);
                        }
                        logger.atDebug().log("Failed to re-authenticate request '{}' with authenticator '{}'", request, authenticator);
                    }
                    return CompletableFuture.completedFuture(response);
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> sendAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest copy = HttpRequest.newBuilder(request, (key, value) -> true)
                .setHeader("Connection", "close")
                .build();
        return new NetworkCompletableFuture<>(copy, bodyHandler);
    }

    private @NotNull HttpRequest createRequest(@NotNull HttpRequest request, @NotNull MultiMap<String, String> requests) {

    }

    @Override
    public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener) {
        logger.atDebug().log("Subscribing to {} with priority {}", event.getResource(), priority);
        SubscriptionEntity entity = new SubscriptionEntity(this, event, priority, listener);
        subscriptionGroup.getEntities().add(entity);
        return subscriptionGroup.update()
                .thenApplyAsync(ignored -> {
                    logger.atDebug().log("Subscribed to {} with priority {}", event.getResource(), priority);
                    return entity;
                });
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribe(@NotNull SubscriptionEntity entity) {
        subscriptionGroup.getEntities().remove(entity);
        return subscriptionGroup.update();
    }

    public @NotNull CompletableFuture<Void> closeAsync() {
        logger.atDebug().log("Closing NetworkClient");
        subscriptionGroup.getEntities().clear();
        return subscriptionGroup.update()
                .thenRunAsync(executorService::shutdownNow);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        logger.atDebug().log("Closing NetworkClient");
        try {
            subscriptionGroup.getEntities().clear();
            subscriptionGroup.update().get();
            executorService.shutdownNow();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException checkedException) throw checkedException;
            if (cause instanceof RuntimeException uncheckedException) throw uncheckedException;
            throw new IllegalStateException(cause);
        }
    }

    public static class CloseableCompletableFuture<T> extends CompletableFuture<T> {
        @Override
        public <U> CompletableFuture<U> newIncompleteFuture() {
            return new CloseableCompletableFuture<>() {
                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    return CloseableCompletableFuture.this.cancel(mayInterruptIfRunning);
                }
            };
        }
    }

    public class NetworkCompletableFuture<T> extends CloseableCompletableFuture<HttpResponse<T>> {

        private final Future<Void> semaphoreAsync;
        private @Nullable CompletableFuture<HttpResponse<T>> requestAsync;

        public NetworkCompletableFuture(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
            semaphoreAsync = executorService.submit(() -> {
                semaphore.acquire();
                requestAsync = httpClient.sendAsync(request, bodyHandler)
                        .thenComposeAsync((response) -> {
                            if (response.statusCode() == 503) {
                                logger.atDebug().log("Retrying request with response'" + response + "'");
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    CompletableFuture<HttpResponse<T>> completableFuture = new CompletableFuture<>();
                                    completableFuture.cancel(false);
                                    return completableFuture;
                                }
                                return httpClient.sendAsync(request, bodyHandler);
                            }
                            return CompletableFuture.completedFuture(response);
                        }, executorService)
                        .handleAsync(((response, throwable) -> {
                            semaphore.release();
                            if (throwable != null) {
                                completeExceptionally(throwable);
                                throw HttpNetworkClient.getThrowable(throwable);
                            }
                            complete(response);
                            return response;
                        }), executorService);
                return null;
            });
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            semaphoreAsync.cancel(mayInterruptIfRunning);
            if (requestAsync != null) {
                requestAsync.cancel(mayInterruptIfRunning);
            }
            return super.cancel(mayInterruptIfRunning);
        }
    }
}
