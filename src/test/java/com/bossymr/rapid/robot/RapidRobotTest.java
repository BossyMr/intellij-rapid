package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.net.URI;

public class RapidRobotTest extends BasePlatformTestCase {

    public void testConnect() throws IOException, InterruptedException {
        RobotService service = RobotService.getInstance();
        service.disconnect();
        assertNull(service.getRobotState());
        assertNull(service.getRobot());
        assertFalse(RobotService.getInstance().isConnected());
        RapidRobot robot = service.connect(URI.create("http://localhost"), RobotService.DEFAULT_CREDENTIALS);
        assertEquals(robot, service.getRobot());
        assertNotNull(service.getRobotState());
        assertTrue(RobotService.getInstance().isConnected());
        assertFalse(robot.getTasks().isEmpty());
    }
}
