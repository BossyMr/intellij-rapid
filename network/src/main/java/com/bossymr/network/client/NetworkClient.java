package com.bossymr.network.client;

import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.network.client.security.impl.DigestAuthenticator;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class NetworkClient {

    private static final Logger logger = LoggerFactory.getLogger(NetworkClient.class);

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
                .addInterceptor(chain -> {
                    Response response = chain.proceed(chain.request());
                    logger.atDebug().log("Request {} -> {}", chain.request(), response);
                    if(response.code() >= 300) {
                        throw new ResponseStatusException(response, response.body().string());
                    }
                    return response;
                })
                .dispatcher(dispatcher)
                .build();
        this.subscriptionGroup = new SubscriptionGroup(this);
    }

    public @NotNull OkHttpClient getHttpClient() {
        return httpClient;
    }

    public @NotNull Response send(@NotNull NetworkRequest request) throws IOException, InterruptedException {
        return send(request.build(defaultPath));
    }

    public @NotNull Response send(@NotNull Request request) throws IOException, InterruptedException {
        return httpClient.newCall(request).execute();
    }

    public @NotNull SubscriptionEntity subscribe(@NotNull SubscribableEvent<?> event, @NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<EntityModel> listener) throws IOException, InterruptedException {
        logger.atDebug().log("Subscribing to '{}' with priority {}", event.getResource(), priority);
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
        subscriptionGroup.update();
        logger.atDebug().log("Subscribed to '{}' with priority {}", event.getResource(), priority);
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
        logger.atDebug().log("Closing NetworkClient");
        subscriptionGroup.getEntities().clear();
        subscriptionGroup.update();
    }
}
