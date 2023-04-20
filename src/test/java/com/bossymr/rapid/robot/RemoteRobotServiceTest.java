package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.robot.network.NetworkTestUtil;
import com.intellij.testFramework.junit5.TestApplication;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.File;
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
        RapidRobot robot = remoteService.connect(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
        RapidTask task = robot.getTask("T_ROB1");
        assertNotNull(task);
        File directory = task.getDirectory();
        assertTrue(directory.exists());
        assertEquals(robot, remoteService.getRobot());
        assertNotNull(remoteService.getRobotState());
        remoteService.disconnect();
        assertFalse(directory.exists());
        assertNull(remoteService.getRobotState());
        assertNull(remoteService.getRobot());
        assertFalse(robot.isConnected());
    }
}