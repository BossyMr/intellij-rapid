package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.network.Controller;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.io.IOException;
import java.net.URI;

public class RobotServiceTest extends BasePlatformTestCase {

    public void testConnect() throws IOException {
        RobotService service = RobotService.getInstance();
        service.disconnect();
        assertNull(service.getRobot());
        Robot robot = service.connect(URI.create("http://localhost:80/"), Controller.DEFAULT_CREDENTIALS);
        assertNotNull(robot);
        assertEquals(robot, service.getRobot());
        assertNotNull(service.getRobotState());
        service.disconnect();
        assertNull(service.getRobot());
        assertNull(service.getRobotState());
    }
}