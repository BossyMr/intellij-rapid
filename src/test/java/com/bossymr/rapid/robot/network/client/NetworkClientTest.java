package com.bossymr.rapid.robot.network.client;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.NetworkTestUtil;
import com.bossymr.rapid.robot.network.ServiceModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.client.model.Model;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.net.http.HttpRequest;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
class NetworkClientTest {

    private static @NotNull NetworkClient getNetworkClient() {
        return NetworkClient.connect(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
    }

    @DisplayName("Test Connect")
    @Test
    void connect() throws Throwable {
        NetworkClient networkClient = getNetworkClient();
        assertEquals(NetworkTestUtil.DEFAULT_PATH, networkClient.getDefaultPath());
        HttpRequest httpRequest = HttpRequest.newBuilder(NetworkTestUtil.DEFAULT_PATH).build();
        networkClient.send(httpRequest);
        networkClient.sendAsync(httpRequest).get();
    }

    @DisplayName("Test Disconnect")
    @Test
    void disconnect() throws Throwable {
        NetworkClient networkClient = getNetworkClient();
        HttpRequest httpRequest = HttpRequest.newBuilder(NetworkTestUtil.DEFAULT_PATH).build();
        networkClient.close();
        CompletableFuture<?> response = networkClient.sendAsync(httpRequest);
        assertThrows(ExecutionException.class, response::get);
    }

    @DisplayName("Test Service Proxy")
    @Test
    void serviceProxy() {
        NetworkClient networkClient = getNetworkClient();
        IsService service = networkClient.newService(IsService.class);
        assertEquals(networkClient, service.getNetworkClient());
    }

    @DisplayName("Test Entity Proxy")
    @Test
    void entityProxy() {
        NetworkClient networkClient = getNetworkClient();
        Model model = new Model("", "entity", Map.of("field", "value"), Map.of());
        IsEntity entity = networkClient.newEntity(model, IsEntity.class);
        assertNotNull(entity);
        assertEquals(networkClient, entity.getNetworkClient());
        assertEquals(Map.of("field", "value"), model.getFields());
        assertEquals("value", model.getField("field"));
        IsSuperEntity superEntity = networkClient.newEntity(model, IsSuperEntity.class);
        assertInstanceOf(IsSubEntity.class, superEntity);
    }

    @Service
    private interface IsService extends ServiceModel {}

    @Entity({"entity"})
    private interface IsEntity extends EntityModel {}

    @Entity(subtype = {IsSubEntity.class})
    private interface IsSuperEntity extends EntityModel {}

    @Entity({"entity"})
    private interface IsSubEntity extends IsSuperEntity {}

}
