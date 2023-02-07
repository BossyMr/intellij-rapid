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
import java.util.HashSet;
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

    private final @NotNull HttpClient httpClient;
    private final @NotNull Authenticator authenticator;
    private final @NotNull ExecutorService executorService;
    private final URI defaultPath;

    private @Nullable SubscriptionGroup group;

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
        logger.atInfo().log("Sending request '{} synchronously with response '{}'", request, response);
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
            return httpClient.send(request, bodyHandler);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest request) {
        return retryAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(response -> {
                    logger.atInfo().log("Sending request '{} synchronously with response '{}'", request, response);
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
        CompletableFuture<HttpResponse<T>> completableFuture = new CompletableFuture<>();
        executorService.submit(() -> {
            semaphore.acquire();
            logger.atDebug().log("Acquired semaphore for request '{}'; available permits: {}; queue length: {}", request, semaphore.availablePermits(), semaphore.getQueueLength());
            httpClient.sendAsync(request, bodyHandler)
                    .handleAsync(((response, throwable) -> {
                        semaphore.release();
                        logger.atDebug().log("Released semaphore for request '{}'", request);
                        if (throwable != null) {
                            completableFuture.completeExceptionally(throwable);
                            throw new CompletionException(throwable);
                        }
                        completableFuture.complete(response);
                        return response;
                    }));
            return null;
        });
        return completableFuture;
    }

    @Override
    public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener) {
        logger.atInfo().log("Subscribing to {} with priority {}", event.getResource(), priority);
        SubscriptionEntity entity = new SubscriptionEntity(this, event, priority, listener);
        if (group != null) {
            group.getEntities().add(entity);
            return group.update().thenApplyAsync(ignored -> entity);
        } else {
            Set<SubscriptionEntity> entities = new HashSet<>();
            entities.add(entity);
            return SubscriptionGroup.start(this, entities, defaultPath)
                    .thenApplyAsync(result -> {
                        this.group = result;
                        return entity;
                    });
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribe(@NotNull SubscriptionEntity entity) {
        if (group != null) {
            if (group.getEntities().remove(entity)) {
                if (group.getEntities().isEmpty()) {
                    return group.close().thenRunAsync(() -> group = null);
                } else {
                    return group.update();
                }
            }
        }
        logger.warn("Attempting to close a previously closed subscription");
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (group != null) {
            try {
                group.close().get();
                executorService.shutdownNow();
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException checkedException) throw checkedException;
                if (cause instanceof RuntimeException uncheckedException) throw uncheckedException;
                throw new IllegalStateException(cause);
            }
        }
    }
}
