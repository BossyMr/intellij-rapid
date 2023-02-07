package com.bossymr.rapid.robot;

import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.robot.impl.RobotUtil;
import com.bossymr.rapid.robot.network.NetworkTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
@TestApplication
class RemoteRobotServiceTest {

    @DisplayName("Test Connect")
    @Test
    public void connect() throws IOException, InterruptedException {
        RemoteRobotService remoteService = RemoteRobotService.getInstance();
        assertNull(remoteService.getRobotState());
        assertNull(remoteService.getRobot());
        Robot robot = remoteService.connect(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
        Credentials credentials = RobotUtil.getCredentials(NetworkTestUtil.DEFAULT_PATH);
        assertNotNull(credentials);
        assertEquals(robot, remoteService.getRobot());
        assertNotNull(remoteService.getRobotState());
        remoteService.disconnect();
        assertNull(remoteService.getRobotState());
        assertNull(remoteService.getRobot());
        assertNull(robot.getNetworkEngine());
    }
}