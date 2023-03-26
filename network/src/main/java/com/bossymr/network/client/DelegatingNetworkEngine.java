package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.SubscriptionEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.HttpRequest;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code NetworkAction} is a {@link NetworkEngine} which delegates requests to an underlying {@link NetworkEngine}.
 * <p>
 * A {@code NetworkAction} represents a policy for handling both successful and unsuccessful responses.
 */
public class DelegatingNetworkEngine extends NetworkEngine {

    private final @NotNull NetworkEngine engine;

    public DelegatingNetworkEngine(@NotNull NetworkEngine engine) {
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
    protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {}

    /**
     * This method is called for each unsuccessful attempt to subscribe to a resource.
     *
     * @param throwable the exception.
     */
    protected void onFailure(@NotNull Throwable throwable) {}

    @Override
    protected @NotNull <T> NetworkCall<T> createNetworkCall(@NotNull NetworkEngine engine, @NotNull HttpRequest request, @NotNull Type returnType) {
        return new DelegatingNetworkCall<>(this, DelegatingNetworkEngine.this.engine.createNetworkCall(engine, request, returnType)) {
            @Override
            protected void onSuccess(@Nullable T response) {
                DelegatingNetworkEngine.this.onSuccess(this, response);
            }

            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                DelegatingNetworkEngine.this.onFailure(this, throwable);
            }
        };
    }

    @Override
    public void close() throws IOException, InterruptedException {
        for (SubscriptionEntity subscription : subscriptions) {
            subscription.unsubscribe();
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> closeAsync() {
        return CompletableFuture.allOf(subscriptions.stream()
                .map(SubscriptionEntity::unsubscribe)
                .toList().toArray(CompletableFuture[]::new));
    }

    @Override
    protected @NotNull <T> SubscribableNetworkCall<T> createSubscribableNetworkCall(@NotNull NetworkEngine engine, @NotNull SubscribableEvent<T> event) {
        return new DelegatingSubscribableNetworkCall<>(engine, DelegatingNetworkEngine.this.engine.createSubscribableNetworkCall(engine, event)) {
            @Override
            protected void onFailure(@NotNull Throwable throwable) {
                DelegatingNetworkEngine.this.onFailure(throwable);
            }
        };
    }

    public static class ShutdownOnFailure extends DelegatingNetworkEngine {

        public ShutdownOnFailure(@NotNull NetworkEngine engine) {
            super(engine);
        }

        @Override
        protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
            try {
                close();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onFailure(@NotNull Throwable throwable) {
            try {
                close();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
