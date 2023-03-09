package com.bossymr.network.client;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
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
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * A {@code HttpNetworkKClient} is an implementation of a {@link NetworkClient} with authentication support, support for
 * enforcing a set amount of max connections.
 */
public class HttpNetworkClient implements NetworkClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpNetworkClient.class);

    private final int MAX_CONNECTIONS = 2;
    private final Semaphore semaphore = new Semaphore(MAX_CONNECTIONS);

    private final @NotNull Authenticator authenticator;
    private final @NotNull ExecutorService executorService;
    private final URI defaultPath;

    private @NotNull HttpClient httpClient;
    private @NotNull SubscriptionGroup subscriptionGroup;

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

    private void build() {
        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
        this.subscriptionGroup.getEntities().clear();
        this.subscriptionGroup.update();
        Set<SubscriptionEntity> entities = this.subscriptionGroup.getEntities();
        this.subscriptionGroup = new SubscriptionGroup(executorService, this);
        this.subscriptionGroup.getEntities().addAll(entities);
        this.subscriptionGroup.update();
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
        logger.atDebug().log("Sending request '{} synchronously with response '{}'", request, response);
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
            HttpResponse<T> response = httpClient.send(request, bodyHandler);
            if (response.statusCode() == 503) {
                logger.atDebug().log("Rebuilding NetworkClient with response'" + response + "'");
                build();
                return httpClient.send(request, bodyHandler);
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
        return new NetworkCompletableFuture<>(request, bodyHandler);
    }

    @Override
    public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener) {
        logger.atDebug().log("Subscribing to {} with priority {}", event.getResource(), priority);
        SubscriptionEntity entity = new SubscriptionEntity(this, event, priority, listener);
        subscriptionGroup.getEntities().add(entity);
        return subscriptionGroup.update()
                .thenApplyAsync(ignored -> entity);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribe(@NotNull SubscriptionEntity entity) {
        subscriptionGroup.getEntities().remove(entity);
        return subscriptionGroup.update();
    }

    @Override
    public void close() throws IOException, InterruptedException {
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
                            /*
                             * If the request fails due to a 503 error (caused by too many connected clients), reconnect and retry.
                             */
                            if (response.statusCode() == 503) {
                                logger.atDebug().log("Rebuilding NetworkClient with response'" + response + "'");
                                build();
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
