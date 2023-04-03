package com.bossymr.network.client;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.security.Authenticator;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.client.security.impl.DigestAuthenticator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.*;

public class NetworkClient {

    private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);

    private static final int MAX_CONNECTIONS = 2;
    private final @NotNull Semaphore semaphore = new Semaphore(MAX_CONNECTIONS);

    private final @NotNull Authenticator authenticator;
    private final @NotNull ExecutorService executorService;
    private final @NotNull URI defaultPath;

    private final @NotNull SubscriptionGroup subscriptionGroup;
    private final @NotNull HttpClient httpClient;

    public NetworkClient(@NotNull URI defaultPath, @NotNull Credentials credentials) {
        this.defaultPath = defaultPath;
        this.authenticator = new DigestAuthenticator(credentials);
        this.executorService = Executors.newCachedThreadPool(Executors.defaultThreadFactory());
        this.httpClient = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
        this.subscriptionGroup = new SubscriptionGroup(this);
    }

    public static <T> @NotNull T computeAsync(@NotNull CompletableFuture<T> completableFuture) throws IOException, InterruptedException {
        try {
            return completableFuture.get();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof IOException exception) {
                throw exception;
            }
            throw new IOException(e);
        }
    }

    public @NotNull RequestBuilder createRequest() {
        return new RequestBuilder(getDefaultPath());
    }

    public @NotNull URI getDefaultPath() {
        return defaultPath;
    }

    public @NotNull HttpClient getHttpClient() {
        return httpClient;
    }

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
            HttpResponse<T> response = httpClient.send(request, bodyHandler);
            if (response.statusCode() == 503) {
                logger.atDebug().log("Retrying request with response'" + response + "'");
                return httpClient.send(request, bodyHandler);
            }
            return response;
        } finally {
            semaphore.release();
        }
    }

    public @NotNull SubscriptionEntity subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<EntityModel> listener) throws IOException, InterruptedException {
        logger.atDebug().log("Subscribing to {} with priority {}", event.getResource(), priority);
        SubscriptionEntity entity = new SubscriptionEntity(event, priority) {
            @Override
            public void unsubscribe() throws IOException, InterruptedException {
                NetworkClient.this.unsubscribe(this);
            }

            @Override
            public void event(@NotNull EntityModel model) {
                listener.onEvent(this, model);
            }
        };
        subscriptionGroup.getEntities().add(entity);
        subscriptionGroup.update();
        logger.atDebug().log("Subscribed to {} with priority {}", event.getResource(), priority);
        return entity;
    }

    public void unsubscribe(@NotNull SubscriptionEntity entity) throws IOException, InterruptedException {
        subscriptionGroup.getEntities().remove(entity);
        subscriptionGroup.update();
    }

    public void close() throws IOException, InterruptedException {
        logger.atDebug().log("Closing NetworkClient");
        subscriptionGroup.getEntities().clear();
        subscriptionGroup.update();
        executorService.shutdownNow();
    }
}
