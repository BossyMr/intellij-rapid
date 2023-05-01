package com.bossymr.network.client;

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
            record SuccessEntity<T>(@NotNull NetworkRequest request, @Nullable T entity) {}
            record FailureEntity(@NotNull NetworkRequest request, @NotNull Throwable throwable) {}

            AtomicReference<SuccessEntity<?>> success = new AtomicReference<>();
            AtomicReference<FailureEntity> failure = new AtomicReference<>();
            NetworkAction action = new NetworkAction(manager) {
                @Override
                protected <T> boolean onSuccess(@NotNull NetworkRequest request, @Nullable T entity) {
                    success.set(new SuccessEntity<>(request, entity));
                    return false;
                }

                @Override
                protected boolean onFailure(@NotNull NetworkRequest request, @NotNull Throwable throwable) {
                    failure.set(new FailureEntity(request, throwable));
                    return false;
                }
            };
            NetworkRequest successRequest = new NetworkRequest()
                    .setPath(URI.create("/success"));
            String entity = action.createQuery(String.class, successRequest).get();
            assertNotNull(success.get());
            assertEquals(successRequest, success.get().request());
            assertEquals(entity, success.get().entity());
            NetworkRequest failureRequest = new NetworkRequest()
                    .setPath(URI.create("/failure"));
            try {
                action.createQuery(String.class, failureRequest).get();
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
                protected <T> boolean onSuccess(@NotNull NetworkRequest request, @Nullable T entity) {
                    throw new IllegalArgumentException();
                }

                @Override
                protected boolean onFailure(@NotNull NetworkRequest request, @NotNull Throwable throwable) {
                    throw new IllegalStateException();
                }
            };
            NetworkRequest successRequest = new NetworkRequest()
                    .setPath(URI.create("/success"));
            assertThrows(IllegalArgumentException.class, () -> action.createQuery(String.class, successRequest).get());
            NetworkRequest failureRequest = new NetworkRequest()
                    .setPath(URI.create("/failure"));
            assertThrows(IllegalStateException.class, () -> action.createQuery(String.class, failureRequest).get());
        }
    }
}
