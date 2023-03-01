package com.bossymr.rapid.robot.network.robotware.mastership;

import com.bossymr.network.NetworkCall;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public abstract class CloseableMastership implements AutoCloseable {

    private volatile boolean closed;

    public static @NotNull CloseableMastership request(@NotNull MastershipService service) throws IOException, InterruptedException {
        service.request().send();
        return new CloseableMastership() {
            @Override
            protected @NotNull NetworkCall<Void> getRequest() {
                return service.release();
            }
        };
    }

    public static @NotNull CloseableMastership request(@NotNull MastershipDomain domain) throws IOException, InterruptedException {
        domain.request().send();
        return new CloseableMastership() {
            @Override
            protected @NotNull NetworkCall<Void> getRequest() {
                return domain.release();
            }
        };
    }

    public static <T> @NotNull CompletableFuture<T> requestAsync(@NotNull MastershipDomain domain, @NotNull Supplier<CompletableFuture<T>> request) {
        return domain.request().sendAsync()
                .thenComposeAsync(ignored -> request.get())
                .handleAsync((response, throwable) -> {
                    domain.release().sendAsync();
                    if (throwable != null) {
                        throw throwable instanceof RuntimeException runtimeException ? runtimeException : new CompletionException(throwable);
                    }
                    return response;
                });

    }

    protected abstract @NotNull NetworkCall<Void> getRequest();

    @Override
    public void close() throws InterruptedException, IOException {
        if (closed) return;
        closed = true;
        getRequest().send();
    }
}
