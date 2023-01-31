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
                return send(retry, bodyHandler);
            }
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
                            return sendAsync(retry, bodyHandler);
                        }
                    }
                    return CompletableFuture.completedFuture(response);
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> sendAsync(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        return CompletableFuture.completedFuture(null)
                .thenComposeAsync(ignored -> {
                    try {
                        semaphore.acquire();
                        return httpClient.sendAsync(request, bodyHandler);
                    } catch (InterruptedException e) {
                        throw new CompletionException(e);
                    }
                })
                .handleAsync((response, throwable) -> {
                    semaphore.release();
                    if (throwable != null) {
                        throw throwable instanceof CompletionException ?
                                ((CompletionException) throwable) :
                                new CompletionException(throwable);
                    }
                    return response;
                });
    }

    @Override
    public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<Model> listener) {
        SubscriptionEntity entity = new SubscriptionEntity(this, event, priority, listener);
        if (group != null) {
            group.getEntities().add(entity);
            return group.update().thenApplyAsync(ignored -> entity);
        } else {
            Set<SubscriptionEntity> entities = new HashSet<>();
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
            group.getEntities().remove(entity);
            if (group.getEntities().isEmpty()) {
                return group.close().thenRunAsync(() -> group = null);
            } else {
                return group.update();
            }
        }
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
