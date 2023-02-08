package com.bossymr.network.client;

import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.SubscriptionEntity;
import com.bossymr.network.SubscriptionListener;
import com.bossymr.network.SubscriptionPriority;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class DelegatingSubscribableNetworkCall<T> extends CloseableSubscribableNetworkCall<T> {

    private final @NotNull SubscribableNetworkCall<T> networkCall;

    public DelegatingSubscribableNetworkCall(@NotNull SubscribableNetworkCall<T> networkCall) {
        this.networkCall = networkCall;
    }

    protected abstract void onFailure(@NotNull Throwable throwable);

    @Override
    protected @NotNull CompletableFuture<SubscriptionEntity> create(@NotNull SubscriptionPriority priority, @NotNull SubscriptionListener<T> listener) {
        return networkCall.subscribe(priority, listener).handleAsync((entity, throwable) -> {
            if (throwable != null) {
                onFailure(throwable);
                throw HttpNetworkClient.getThrowable(throwable);
            }
            return entity;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegatingSubscribableNetworkCall<?> that = (DelegatingSubscribableNetworkCall<?>) o;
        return networkCall.equals(that.networkCall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkCall);
    }

    @Override
    public String toString() {
        return "DelegatingSubscribableNetworkCall{" +
                "networkCall=" + networkCall +
                '}';
    }
}
