package com.bossymr.rapid.robot.api.client;

import com.bossymr.rapid.robot.api.ResponseStatusException;
import com.bossymr.rapid.robot.api.SubscriptionEntity;
import com.bossymr.rapid.robot.api.SubscriptionListener;
import com.bossymr.rapid.robot.api.SubscriptionPriority;
import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.api.client.security.impl.DigestAuthenticator;
import com.intellij.openapi.diagnostic.Logger;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class NetworkClient {

    private static final Logger logger = Logger.getInstance(NetworkClient.class);

    private final @NotNull SubscriptionGroup subscriptionGroup;
    private final @NotNull OkHttpClient httpClient;

    private final @NotNull URI defaultPath;

    public NetworkClient(@NotNull URI defaultPath, @Nullable Credentials credentials) {
        this.defaultPath = defaultPath;
        CookieJar cookieJar = new CookieJar() {
            private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

            @Override
            public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public @NotNull List<Cookie> loadForRequest(@NotNull HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        };
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequestsPerHost(1);
        this.httpClient = new OkHttpClient.Builder()
                .authenticator(new DigestAuthenticator(credentials))
                .cookieJar(cookieJar)
                .callTimeout(Duration.ofSeconds(10))
                .writeTimeout(Duration.ofSeconds(10))
                .readTimeout(Duration.ofSeconds(30))
                .addInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    logger.debug("Request {} -> {}", chain.request(), response);
                    if(response.code() >= 300) {
                        throw new ResponseStatusException(response, response.body().string());
                    }
                    return response;
                })
                .dispatcher(dispatcher)
                .build();
        this.subscriptionGroup = new SubscriptionGroup(this);
    }

    public @NotNull URI getDefaultPath() {
        return defaultPath;
    }

    public @NotNull OkHttpClient getHttpClient() {
        return httpClient;
    }

    public @NotNull Response send(@NotNull NetworkRequest<?> request) throws IOException, InterruptedException {
        return send(request.build(defaultPath));
    }

    public @NotNull Response send(@NotNull Request request) throws IOException, InterruptedException {
        return httpClient.newCall(request).execute();
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
