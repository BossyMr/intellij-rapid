package com.bossymr.network.client;

import com.bossymr.network.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

/**
 * A {@code NetworkAction} is a {@link NetworkEngine} which delegates requests to an underlying {@link NetworkEngine}.
 * <p>
 * A {@code NetworkAction} represents a policy for handling both successful and unsuccessful responses.
 */
public class NetworkAction extends NetworkEngine {

    private final Set<NetworkCall<?>> requests = ConcurrentHashMap.newKeySet();
    private final Set<SubscribableNetworkCall<?>> subscriptions = ConcurrentHashMap.newKeySet();

    private final @NotNull NetworkEngine engine;
    private boolean closed;

    private NetworkAction(@NotNull NetworkEngine engine) {
        super(engine.getNetworkClient(), engine.getEntityFactory(), engine.getRequestFactory());
        this.engine = engine;
    }

    @Override
    public @NotNull NetworkEngine getNetworkEngine() {
        return this;
    }

    @Override
    public @NotNull EntityFactory getEntityFactory() {
        return new EntityFactory(this, getNetworkClient());
    }

    @Override
    public @NotNull RequestFactory getRequestFactory() {
        return new RequestFactory(this);
    }

    /**
     * This method is called for each successful response.
     *
     * @param request the request.
     * @param response the response.
     * @param <T> the response type.
     */
    protected <T> void onSuccess(@NotNull NetworkCall<T> request, @Nullable T response) {}

    /**
     * This method is called for each unsuccessful response.
     *
     * @param request the request.
     * @param throwable the exception.
     */
    protected void onFailure(@Nullable NetworkCall<?> request, @NotNull Throwable throwable) {}

    /**
     * This method is called for each unsuccessful attempt to subscribe to a resource.
     *
     * @param throwable the exception.
     */
    protected void onFailure(@NotNull Throwable throwable) {}

    @Override
    protected @NotNull <T> NetworkCall<T> createNetworkCall(@NotNull NetworkEngine engine, @NotNull HttpRequest request, @NotNull Type returnType) {
        if (closed) throw new IllegalArgumentException();
        DelegatingNetworkCall<T> networkCall = new DelegatingNetworkCall<>(this.engine.createNetworkCall(engine, request, returnType)) {
            @Override
            protected void onSuccess(@Nullable T response) {
                NetworkAction.this.onSuccess(this, response);
            }

            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                NetworkAction.this.onFailure(this, throwable);
            }
        };
        requests.add(networkCall);
        return networkCall;
    }

    @Override
    protected @NotNull <T> SubscribableNetworkCall<T> createSubscribableNetworkCall(@NotNull NetworkEngine engine, @NotNull SubscribableEvent<T> event) {
        if (closed) throw new IllegalArgumentException();
        DelegatingSubscribableNetworkCall<T> networkCall = new DelegatingSubscribableNetworkCall<>(this.engine.createSubscribableNetworkCall(engine, event)) {
            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                NetworkAction.this.onFailure(throwable);
            }
        };
        subscriptions.remove(networkCall);
        return networkCall;
    }

    @Override
    public void close() throws IOException, InterruptedException {
        if (closed) return;
        closed = true;
        requests.forEach(NetworkCall::close);
        for (SubscribableNetworkCall<?> subscription : subscriptions) {
            subscription.close();
        }
    }

    private static class DelegatingNetworkCall<T> implements NetworkCall<T> {

        private final @NotNull Set<CompletableFuture<?>> requests = ConcurrentHashMap.newKeySet();
        private final @NotNull NetworkCall<T> networkCall;

        private boolean closed;

        public DelegatingNetworkCall(@NotNull NetworkCall<T> networkCall) {
            this.networkCall = networkCall;
        }

        protected void onSuccess(@Nullable T response) {}

        protected void onFailure(@NotNull Throwable throwable) {}

        @Override
        public @Nullable T send() throws IOException, InterruptedException {
            if (closed) throw new IllegalArgumentException();
            try {
                T response = networkCall.send();
                onSuccess(response);
                return response;
            } catch (RuntimeException | IOException | InterruptedException e) {
                onFailure(e);
                throw e;
            }
        }

        @Override
        public @NotNull CompletableFuture<T> sendAsync() {
            if (closed) throw new IllegalArgumentException();
            CompletableFuture<T> request = networkCall.sendAsync();
            requests.add(request);
            return request
                    .handleAsync((response, throwable) -> {
                        requests.remove(request);
                        if (throwable != null) {
                            onFailure(throwable);
                            throw new CompletionException(throwable);
                        }
                        onSuccess(response);
                        return response;
                    });
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            requests.forEach(request -> request.cancel(true));
        }
    }

    private static class DelegatingSubscribableNetworkCall<T> implements SubscribableNetworkCall<T> {

        private final @NotNull Set<SubscriptionEntity> entities = ConcurrentHashMap.newKeySet();
        private final @NotNull SubscribableNetworkCall<T> networkCall;
        private boolean closed;

        public DelegatingSubscribableNetworkCall(@NotNull SubscribableNetworkCall<T> networkCall) {
            this.networkCall = networkCall;
        }

        protected void onFailure(@NotNull Throwable throwable) {}

        @Override
        public @NotNull CompletableFuture<SubscriptionEntity> subscribe(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener) {
            if (closed) throw new IllegalArgumentException();
            return networkCall.subscribe(priority, new SubscriptionListener<T>() {
                        @Override
                        public void onEvent(@NotNull SubscriptionEntity entity, @NotNull T event) {
                            listener.onEvent(entity, event);
                        }

                        @Override
                        public void onUnsubscribe(@NotNull SubscriptionEntity entity) {
                            listener.onUnsubscribe(entity);
                            entities.remove(entity);
                        }
                    })
                    .handleAsync((entity, throwable) -> {
                        entities.add(entity);
                        if (throwable != null) {
                            onFailure(throwable);
                            throw new CompletionException(throwable);
                        }
                        entities.add(entity);
                        return entity;
                    });
        }

        @Override
        public void close() throws IOException, InterruptedException {
            if (closed) return;
            this.closed = true;
            for (SubscriptionEntity entity : entities) {
                try {
                    entity.unsubscribe().get();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof IOException checkedException) throw checkedException;
                    if (cause instanceof RuntimeException uncheckedException) throw uncheckedException;
                    throw new IllegalStateException(cause);
                }
            }
        }
    }
}
