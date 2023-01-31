package com.bossymr.network.client;

import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class HttpSubscribableNetworkCall<T> extends CloseableSubscribableNetworkCall<T> {

    private final @NotNull NetworkEngine engine;
    private final @NotNull SubscribableEvent<T> event;

    public HttpSubscribableNetworkCall(@NotNull NetworkEngine engine, @NotNull SubscribableEvent<T> event) {
        this.engine = engine;
        this.event = event;
    }

    @Override
    protected @NotNull CompletableFuture<SubscriptionEntity> create(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener) {
        return engine.getNetworkClient().subscribe(event, priority, (entity, model) -> {
            T response = engine.createEntity(event.getEventType(), model);
            if (response != null) {
                listener.onEvent(entity, response);
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HttpSubscribableNetworkCall<?> that = (HttpSubscribableNetworkCall<?>) o;
        return engine.equals(that.engine) && event.equals(that.event);
    }

    @Override
    public int hashCode() {
        return Objects.hash(engine, event);
    }

    @Override
    public String toString() {
        return "HttpSubscribableNetworkCall{" +
                "engine=" + engine +
                ", event=" + event +
                '}';
    }
}
