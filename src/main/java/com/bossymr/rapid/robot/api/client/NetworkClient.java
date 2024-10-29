package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.ResponseStatusException;
import com.bossymr.rapid.robot.api.SubscriptionEntity;
import com.bossymr.rapid.robot.api.SubscriptionListener;
import com.bossymr.rapid.robot.api.SubscriptionPriority;
import com.bossymr.rapid.robot.api.client.entity.EntityModel;
import com.bossymr.rapid.robot.api.client.security.Authenticator;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.api.client.security.DigestAuthenticator;
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
import java.util.Collection;

public class NetworkClient {

    private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);
    private final @Nullable Authenticator authenticator;

    private final HttpClient client;
    private final URI basePath;
    private final SubscriptionGroup subscriptionGroup;

    public NetworkClient(@NotNull URI basePath, @Nullable Credentials credentials) {
        this.basePath = basePath;
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .cookieHandler(new CookieManager())
                .build();
        if (credentials != null) {
            this.authenticator = new DigestAuthenticator(credentials);
        } else {
            this.authenticator = null;
        }
        this.subscriptionGroup = new SubscriptionGroup(this, client);
    }

    public @NotNull URI getBasePath() {
        return basePath;
    }

    public @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = send(request, HttpResponse.BodyHandlers.ofByteArray());
        if(response.statusCode() >= 300) {
            throw new ResponseStatusException(response);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> send(@NotNull HttpRequest request, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        // Try to preemptively authenticate the request
        if (authenticator != null) {
            request = authenticate(request);
        }
        HttpResponse<T> response = client.send(request, bodyHandler);
        // Check if the request needs to be authenticated
        if (response.statusCode() != 401 && response.statusCode() != 407) {
            return response;
        }
        if (authenticator != null) {
            // Try to reauthenticate the request
            request = authenticator.authenticate(response);
            if (request != null) {
                // The request could be authenticated
                return client.send(request, bodyHandler);
            }
        }
        return response;
    }

    private @NotNull HttpRequest authenticate(@NotNull HttpRequest request) {
        if (authenticator == null) {
            return request;
        }
        HttpRequest httpRequest = authenticator.authenticate(request);
        return httpRequest != null ? httpRequest : request;
    }

    public @NotNull SubscriptionEntity subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<EntityModel> listener) throws IOException, InterruptedException {
        logger.debug("Subscribing to '{}' with priority {}", event.getResource(), priority);
        SubscriptionEntity entity = new SubscriptionEntity(this, event, priority) {

            @Override
            public void unsubscribe() throws IOException, InterruptedException {
                NetworkClient.this.unsubscribe(this);
                listener.onClose(this);
            }

            @Override
            public void event(@NotNull EntityModel model) {
                listener.onEvent(this, model);
            }
        };
        subscriptionGroup.getEntities().add(entity);
        try {
            subscriptionGroup.update();
        } catch (IOException | InterruptedException | RuntimeException e) {
            subscriptionGroup.getEntities().remove(entity);
            throw e;
        }
        logger.debug("Subscribed to '{}' with priority {}", event.getResource(), priority);
        return entity;
    }

    public void unsubscribe(@NotNull SubscriptionEntity entity) throws IOException, InterruptedException {
        if (!(subscriptionGroup.getEntities().remove(entity))) {
            throw new IllegalArgumentException("Entity '" + entity + "' is not subscribed");
        }
        subscriptionGroup.update();
    }

    public void unsubscribe(@NotNull Collection<SubscriptionEntity> entities) throws IOException, InterruptedException {
        for (SubscriptionEntity entity : entities) {
            if (!(subscriptionGroup.getEntities().remove(entity))) {
                throw new IllegalArgumentException("Entity '" + entity + "' is not subscribed");
            }
        }
        subscriptionGroup.update();
    }

    public void close() throws IOException, InterruptedException {
        logger.debug("Closing NetworkClient");
        subscriptionGroup.getEntities().clear();
        subscriptionGroup.update();
    }
}
