package com.bossymr.network.client;

import com.bossymr.network.GenericType;
import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
class NetworkActionTest {

    @Test
    void onSuccessOrFailure(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/success").willReturn(okForContentType("text/plain", "Hello, World!")));
        wireMock.register(get("/failure").willReturn(badRequest()));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            record SuccessEntity<T>(@NotNull NetworkRequest<T> request, @Nullable T entity) {}
            record FailureEntity(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) {}

            AtomicReference<SuccessEntity<?>> success = new AtomicReference<>();
            AtomicReference<FailureEntity> failure = new AtomicReference<>();
            NetworkAction action = new NetworkAction(manager) {
                @Override
                protected <T> boolean onSuccess(@NotNull NetworkRequest<T> request, @Nullable T entity) {
                    success.set(new SuccessEntity<>(request, entity));
                    return false;
                }

                @Override
                protected boolean onFailure(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) {
                    failure.set(new FailureEntity(request, throwable));
                    return false;
                }
            };
            NetworkRequest<String> successRequest = new NetworkRequest<>(URI.create("/success"), GenericType.of(String.class));
            String entity = action.createQuery(successRequest).get();
            assertNotNull(success.get());
            assertEquals(successRequest, success.get().request());
            assertEquals(entity, success.get().entity());
            NetworkRequest<String> failureRequest = new NetworkRequest<>(URI.create("/failure"), GenericType.of(String.class));
            try {
                action.createQuery(failureRequest).get();
                fail();
            } catch (IOException e) {
                assertNotNull(failure.get());
                assertEquals(failureRequest, failure.get().request());
                assertEquals(e, failure.get().throwable());
            }
        }
    }

    @Test
    void onSuccessOrFailureException(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        wireMock.register(get("/success").willReturn(okForContentType("text/plain", "Hello, World!")));
        wireMock.register(get("/failure").willReturn(badRequest()));
        try (NetworkManager manager = new HeavyNetworkManager(URI.create(runtimeInfo.getHttpBaseUrl()), null)) {
            NetworkAction action = new NetworkAction(manager) {
                @Override
                protected <T> boolean onSuccess(@NotNull NetworkRequest<T> request, @Nullable T entity) {
                    throw new IllegalArgumentException();
                }

                @Override
                protected boolean onFailure(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) {
                    throw new IllegalStateException(throwable);
                }
            };
            NetworkRequest<String> successRequest = new NetworkRequest<>(URI.create("/success"), GenericType.of(String.class));
            try {
                action.createQuery(successRequest).get();
            } catch (IllegalStateException e) {
                // As the request will throw an exception if successful, the onFailure handler will also be called.
                assertInstanceOf(IllegalArgumentException.class, e.getCause());
            }
            NetworkRequest<String> failureRequest = new NetworkRequest<>(URI.create("/failure"), GenericType.of(String.class));
            assertThrows(IllegalStateException.class, () -> action.createQuery(failureRequest).get());
        }
    }
}
