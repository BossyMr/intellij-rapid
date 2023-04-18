package com.bossymr.rapid.robot.network;

import com.bossymr.network.client.NetworkAction;
import com.bossymr.network.client.NetworkManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
class RobotServiceTest {

    private static RobotService robotService;

    @BeforeAll
    static void beforeAll() throws IOException, InterruptedException {
        NetworkManager manager = new NetworkManager(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
        try (NetworkAction action = manager.createAction()) {
            robotService = action.createService(RobotService.class);
        }
    }

    @DisplayName("Test Controller Service")
    @Test
    void testControllerService() throws IOException, InterruptedException {
        Identity identity = robotService.getControllerService().getIdentity().get();
        assertNotNull(identity.getName());
    }
}