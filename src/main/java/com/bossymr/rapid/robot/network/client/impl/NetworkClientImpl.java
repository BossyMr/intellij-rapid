package com.bossymr.rapid.robot.network.client.impl;

import com.bossymr.rapid.robot.ResponseStatusException;
import com.bossymr.rapid.robot.network.client.EntityInvocationHandler;
import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.client.ServiceInvocationHandler;
import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.client.model.ModelUtil;
import com.bossymr.rapid.robot.network.client.security.Authenticator;
import com.bossymr.rapid.robot.network.client.security.impl.DigestAuthenticator;
import com.bossymr.rapid.robot.network.query.AsynchronousQuery;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscriptionPriority;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

public class NetworkClientImpl implements NetworkClient {

    private static final Logger LOG = Logger.getInstance(NetworkClientImpl.class);

    private static final CookieManager COOKIE_MANAGER = new CookieManager();

    private final Map<Object, SubscriptionDetail<?>> details = new HashMap<>();
    private final Authenticator authenticator;
    private final HttpClient httpClient;
    private final URI defaultPath;
    private URI subscriptionGroup;
    private WebSocket webSocket;

    public NetworkClientImpl(@NotNull URI defaultPath, @NotNull Credentials credentials) {
        this.authenticator = new DigestAuthenticator(() -> credentials);
        this.defaultPath = defaultPath;
        // By using a new CookieManager instance for each NetworkClient, all cookies are stored in the same place,
        // meaning that session and authentication cookies are reused for a new NetworkClient, with a potentially
        // different NetworkClient. Additionally, only a single NetworkClient should be instantiated at once.
        COOKIE_MANAGER.getCookieStore().removeAll();
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(COOKIE_MANAGER)
                .build();
    }

    @Override
    public @NotNull URI getDefaultPath() {
        return defaultPath;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @NotNull T newService(@NotNull Class<T> serviceType) {
        return (T) Proxy.newProxyInstance(
                serviceType.getClassLoader(),
                new Class[]{serviceType},
                new ServiceInvocationHandler(serviceType, this));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull T newEntity(@NotNull Model model, @NotNull Class<T> entityType) {
        return (T) Proxy.newProxyInstance(
                entityType.getClassLoader(),
                new Class[]{entityType},
                new EntityInvocationHandler(entityType, this, model));
    }

    @Override
    public @NotNull HttpResponse<byte[]> send(@NotNull HttpRequest httpRequest) throws IOException, InterruptedException {
        HttpResponse<byte[]> response = retry(httpRequest, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() >= 300) {
            throw new ResponseStatusException(response);
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> retry(@NotNull HttpRequest httpRequest, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        HttpRequest authenticated = authenticator.authenticate(httpRequest);
        HttpResponse<T> response = notify(authenticated != null ? authenticated : httpRequest, bodyHandler);
        if (response.statusCode() == 401 || response.statusCode() == 407) {
            HttpRequest retry = authenticator.authenticate(response);
            if (retry != null) {
                return notify(retry, bodyHandler);
            }
        }
        return response;
    }

    private <T> @NotNull HttpResponse<T> notify(@NotNull HttpRequest httpRequest, @NotNull HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        HttpResponse<T> response = httpClient.send(httpRequest, bodyHandler);
        LOG.info("Request: '" + httpRequest + "' - Response: '" + response + "'");
        return response;
    }

    @Override
    public @NotNull CompletableFuture<HttpResponse<byte[]>> sendAsync(@NotNull HttpRequest httpRequest) {
        return retryAsync(httpRequest, HttpResponse.BodyHandlers.ofByteArray())
                .thenApplyAsync(response -> {
                    if (response.statusCode() >= 300) {
                        try {
                            throw new ResponseStatusException(response);
                        } catch (IOException e) {
                            throw new CompletionException(e);
                        }
                    }
                    return response;
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> retryAsync(@NotNull HttpRequest httpRequest, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        HttpRequest authenticated = authenticator.authenticate(httpRequest);
        return httpClient.sendAsync(authenticated != null ? authenticated : httpRequest, bodyHandler)
                .thenComposeAsync(response -> {
                    if (response.statusCode() == 401 || response.statusCode() == 407) {
                        HttpRequest retry = authenticator.authenticate(response);
                        if (retry != null) {
                            return notifyAsync(retry, bodyHandler);
                        }
                    }
                    return CompletableFuture.completedFuture(response);
                });
    }

    private <T> @NotNull CompletableFuture<HttpResponse<T>> notifyAsync(@NotNull HttpRequest httpRequest, @NotNull HttpResponse.BodyHandler<T> bodyHandler) {
        return httpClient.sendAsync(httpRequest, bodyHandler)
                .thenApplyAsync(response -> {
                    LOG.info("Request: '" + httpRequest + "' - Response: '" + response + "'");
                    return response;
                });
    }

    public @NotNull String toBody() {
        StringJoiner stringJoiner = new StringJoiner("&");
        List<SubscriptionDetail<?>> values = new ArrayList<>(details.values());
        Map<Object, SubscriptionDetail<?>> subscriptions = new HashMap<>();
        for (SubscriptionDetail<?> value : values) {
            if (subscriptions.containsKey(value.resource())) {
                SubscriptionDetail<?> cached = subscriptions.get(value.resource());
                if (value.priority().ordinal() <= cached.priority().ordinal()) {
                    continue;
                }
            }
            subscriptions.put(value.resource(), value);
        }
        List<SubscriptionDetail<?>> details = new ArrayList<>(subscriptions.values());
        for (int i = 0; i < details.size(); i++) {
            SubscriptionDetail<?> subscriptionDetail = details.get(i);
            stringJoiner.add("resources=" + i);
            stringJoiner.add(i + "=" + subscriptionDetail.resource());
            stringJoiner.add(i + "-p=" + subscriptionDetail.priority().ordinal());
        }
        return stringJoiner.toString();
    }

    @Override
    public <T> void subscribe(@NotNull Object key, @NotNull String resource, @NotNull SubscriptionPriority priority, @NotNull Consumer<T> onEvent, @NotNull Class<T> returnType) throws IOException, InterruptedException {
        if (details.containsKey(key)) {
            throw new IllegalArgumentException();
        }
        details.put(key, new SubscriptionDetail<>(resource, priority, onEvent, returnType));
        if (details.size() == 1) {
            startSubscription();
        } else {
            updateSubscription();
        }
    }

    @Override
    public void unsubscribe(@NotNull Object key) throws IOException, InterruptedException {
        if (!details.containsKey(key)) {
            throw new IllegalArgumentException();
        }
        details.remove(key);
        if (details.isEmpty()) {
            closeSubscription();
        } else {
            updateSubscription();
        }
    }

    private void startSubscription() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(getDefaultPath().resolve("/subscription"))
                .POST(HttpRequest.BodyPublishers.ofString(toBody()))
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpResponse<byte[]> httpResponse = send(httpRequest);
        CollectionModel collectionModel = ModelUtil.convert(httpResponse.body());
        for (Model model : collectionModel.getModels()) {
            handleEntity(model);
        }
        String location = httpResponse.headers().firstValue("Location").orElseThrow();
        URI path = URI.create(location);
        subscriptionGroup = collectionModel.getLink("group");
        webSocket = httpClient.newWebSocketBuilder()
                .subprotocols("robapi2_subscription")
                .buildAsync(path, new EventWebSocketListener(this))
                .join();
    }

    private void closeSubscription() throws IOException, InterruptedException {
        try {
            HttpRequest httpRequest = HttpRequest.newBuilder(subscriptionGroup)
                    .DELETE()
                    .build();
            send(httpRequest);
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "");
        } finally {
            webSocket = null;
            subscriptionGroup = null;
        }
    }

    private void updateSubscription() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder(subscriptionGroup)
                .PUT(HttpRequest.BodyPublishers.ofString(toBody()))
                .setHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        send(httpRequest);
    }

    public void handleEntity(@NotNull Model model) {
        String path = model.getLink("self").getPath();
        for (SubscriptionDetail<?> detail : details.values()) {
            if (path.startsWith(detail.resource())) {
                Map<String, Class<?>> entities = EntityUtil.getReturnTypes(detail.returnType());
                if (entities.containsKey(model.getType())) {
                    handleEntity(model, detail);
                }
            }
        }
    }

    public <T> void handleEntity(@NotNull Model model, @NotNull SubscriptionDetail<T> detail) {
        T entity = newEntity(model, detail.returnType());
        detail.callback().accept(entity);
    }

    @Override
    public @NotNull <T> Query<T> newQuery(@NotNull HttpRequest httpRequest, @NotNull Type returnType) {
        return new QueryImpl<>(this, httpRequest, returnType);
    }

    @Override
    public @NotNull AsynchronousQuery newAsynchronousQuery(@NotNull HttpRequest httpRequest) {
        return new AsynchronousQueryImpl(this, httpRequest);
    }

    @Override
    public @NotNull <T> SubscribableQuery<T> newSubscribableQuery(@NotNull String path, @NotNull Class<T> returnType) {
        return new SubscribableQueryImpl<>(this, path, returnType);
    }

    private record SubscriptionDetail<T>(
            @NotNull String resource,
            @NotNull SubscriptionPriority priority,
            @NotNull Consumer<T> callback,
            @NotNull Class<T> returnType
    ) {}
}
