package com.bossymr.network.client;

import com.bossymr.network.EntityModel;
import com.bossymr.network.ModelTestUtil;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.ServiceModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Property;
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
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.junit.jupiter.api.Assertions.*;

@WireMockTest
public class NetworkEngineTest {

    @Test
    void createService(@NotNull WireMockRuntimeInfo runtimeInfo) throws IOException, InterruptedException {
        WireMock wireMock = runtimeInfo.getWireMock();
        CollectionModel original = new CollectionModelBuilder()
                .addModel()
                .setTitle("title")
                .setType("entity-li")
                .addLink("self", "/default/entity")
                .addField("property", "1")
                .build().build();
        CollectionModel complete = new CollectionModelBuilder()
                .addModel()
                .setTitle("title")
                .setType("entity")
                .addLink("self", "/default/entity")
                .addField("property", "1")
                .addField("complete", "2")
                .build().build();
        wireMock.register(get("/default/entities").willReturn(ModelTestUtil.response(URI.create(runtimeInfo.getHttpBaseUrl()), original)));
        wireMock.register(get("/default/entity").willReturn(ModelTestUtil.response(URI.create(runtimeInfo.getHttpBaseUrl()), complete)));
        try (NetworkEngine networkEngine = new NetworkEngine(runtimeInfo.getHttpBaseUrl(), () -> null)) {
            MyService service = networkEngine.createService(MyService.class);
            NetworkCall<List<MyEntityModel>> networkCall = service.getEntities();
            List<MyEntityModel> entityModel = networkCall.send();
            assertNotNull(entityModel);
            assertEquals(1, entityModel.size());
            MyEntityModel myEntityModel = entityModel.get(0);
            assertEquals("entity-li", myEntityModel.getType());
            assertEquals("title", myEntityModel.getTitle());
            assertEquals(1, myEntityModel.getAsInteger());
            assertEquals("1", myEntityModel.getAsString());
            assertNull(myEntityModel.getMissing());
            assertNotNull(myEntityModel.getComplete());
            assertEquals("entity", myEntityModel.getType());
        }
    }

    @Service("/default/")
    public interface MyService extends ServiceModel {

        @GET("entities")
        @NotNull NetworkCall<List<MyEntityModel>> getEntities();

    }

    @Entity({"entity", "entity-li"})
    public interface MyEntityModel extends EntityModel {

        @Property("property")
        @NotNull String getAsString();

        @Property("property")
        int getAsInteger();

        @Property("missing")
        @Nullable String getMissing();

        @Property("complete")
        @NotNull String getComplete();

    }


}
