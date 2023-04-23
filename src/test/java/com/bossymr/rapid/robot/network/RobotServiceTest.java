package com.bossymr.rapid.robot.network;

import com.bossymr.network.client.HeavyNetworkManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
class RobotServiceTest {

    private static RootService robotService;
    private static HeavyNetworkManager manager;

    @BeforeAll
    static void beforeAll() {
        manager = new HeavyNetworkManager(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
        robotService = manager.createLight().createService(RootService.class);
    }

    @AfterAll
    static void afterAll() throws IOException, InterruptedException {
        manager.close();
    }

    @DisplayName("Test Controller Service")
    @Test
    void testControllerService() throws IOException, InterruptedException {
        Identity identity = robotService.getControllerService().getIdentity().get();
        assertNotNull(identity.getName());
    }
}