package com.bossymr.network.client;

import com.bossymr.network.NetworkCall;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class DelegatingNetworkCall<T> extends CloseableNetworkCall<T> {

    private final @NotNull NetworkCall<T> networkCall;

    protected DelegatingNetworkCall(@NotNull NetworkEngine engine, @NotNull NetworkCall<T> networkCall) {
        super(engine);
        this.networkCall = networkCall;
    }

    @Override
    public @NotNull HttpRequest request() {
        return networkCall.request();
    }

    protected abstract void onSuccess(@Nullable T response);

    protected abstract void onFailure(@NotNull Throwable throwable);

    @Override
    protected @Nullable T create() throws IOException, InterruptedException {
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
    protected @NotNull CompletableFuture<T> createAsync() {
        return networkCall.sendAsync().handleAsync((response, throwable) -> {
            if (throwable != null) {
                onFailure(throwable);
                throw HttpNetworkClient.getThrowable(throwable);
            }
            onSuccess(response);
            return response;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelegatingNetworkCall<?> that = (DelegatingNetworkCall<?>) o;
        return networkCall.equals(that.networkCall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(networkCall);
    }

    @Override
    public String toString() {
        return "DelegatingNetworkCall{" +
                "networkCall=" + networkCall +
                '}';
    }
}
