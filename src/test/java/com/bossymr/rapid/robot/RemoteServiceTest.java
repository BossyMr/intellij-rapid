package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.impl.RobotUtil;
import com.bossymr.rapid.robot.network.NetworkTestUtil;
import com.intellij.testFramework.LightIdeaTestCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;

@EnabledIf("com.bossymr.rapid.robot.network.NetworkTestUtil#doNetworkTest")
public class RemoteServiceTest extends LightIdeaTestCase {

    @DisplayName("Test Connect")
    public void testConnect() throws IOException, InterruptedException {
        RemoteService remoteService = RemoteService.getInstance();
        assertNull(remoteService.getRobotState());
        assertNull(remoteService.getRobot());
        Robot robot = remoteService.connect(NetworkTestUtil.DEFAULT_PATH, NetworkTestUtil.DEFAULT_CREDENTIALS);
        assertEquals(NetworkTestUtil.DEFAULT_CREDENTIALS, RobotUtil.getCredentials(NetworkTestUtil.DEFAULT_PATH));
        assertEquals(robot, remoteService.getRobot());
        assertNotNull(remoteService.getRobotState());
        remoteService.disconnect();
        assertNull(remoteService.getRobotState());
        assertNull(remoteService.getRobot());
        assertNull(robot.getRobotService());
    }
}