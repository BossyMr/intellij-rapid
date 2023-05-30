package com.bossymr.rapid.robot;

import com.bossymr.network.client.security.Credentials;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.net.URI;

public class RapidRobotTest extends BasePlatformTestCase {

    public void testConnect() throws IOException, InterruptedException {
        RobotService service = RobotService.getInstance();
        service.disconnect();
        assertNull(service.getRobotState());
        assertNull(service.getRobot());
        assertFalse(RobotService.isConnected());
        RapidRobot robot = service.connect(URI.create("http://localhost"), new Credentials("Default User", "robotics".toCharArray()));
        assertEquals(robot, service.getRobot());
        assertNotNull(service.getRobotState());
        assertTrue(RobotService.isConnected());
        assertFalse(robot.getTasks().isEmpty());
    }
}
