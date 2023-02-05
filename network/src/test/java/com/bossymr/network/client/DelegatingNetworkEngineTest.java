package com.bossymr.network.client;

import com.bossymr.network.*;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Service;
import com.bossymr.network.model.CollectionModel;
import com.bossymr.network.model.CollectionModelBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.badRequest;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
public class DelegatingNetworkEngineTest {

    @Test
    void policy(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        CollectionModel collectionModel = new CollectionModelBuilder()
                .addModel()
                .setType("entity")
                .addLink("success", "/success")
                .addLink("fail", "/fail")
                .build().build();
        wireMock.register(get("/success").willReturn(ModelTestUtil.response(URI.create(runtimeInfo.getHttpBaseUrl()), collectionModel)));
        wireMock.register(get("/fail").willReturn(badRequest()));
        try (NetworkEngine networkEngine = NetworkEngine.connect(runtimeInfo.getHttpBaseUrl(), () -> null)) {
            final int[] counts = new int[2];
            final DelegatingNetworkEngine delegatingNetworkEngine = new DelegatingNetworkEngine(networkEngine) {
                @Override
                protected <T> void onSuccess(@NotNull NetworkCall<T> request, @Nullable T response) {
                    counts[0]++;
                    super.onSuccess(request, response);
                }

                @Override
                protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
                    counts[1]++;
                    super.onFailure(request, throwable);
                }
            };
            try (delegatingNetworkEngine) {
                MyService myService = delegatingNetworkEngine.createService(MyService.class);
                MyEntity myEntity = myService.success().send();
                assertNotNull(myEntity);
                assertArrayEquals(new int[]{1, 0}, counts);
                assertThrows(ResponseStatusException.class, () -> myService.fail().send());
                assertArrayEquals(new int[]{1, 1}, counts);
                myEntity.success().send();
                assertArrayEquals(new int[]{2, 1}, counts);
                assertThrows(ResponseStatusException.class, () -> myEntity.fail().send());
                assertArrayEquals(new int[]{2, 2}, counts);
            }
        }
    }

    @Service("/")
    public interface MyService extends ServiceModel {

        @GET("success")
        @NotNull NetworkCall<MyEntity> success();

        @GET("fail")
        @NotNull NetworkCall<Void> fail();
    }

    @Entity("entity")
    public interface MyEntity extends EntityModel {

        @GET("{@success}")
        @NotNull NetworkCall<MyEntity> success();

        @GET("{@fail}")
        @NotNull NetworkCall<Void> fail();

    }
}
