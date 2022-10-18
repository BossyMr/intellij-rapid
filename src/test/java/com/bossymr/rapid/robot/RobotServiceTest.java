package com.bossymr.rapid.robot;

import com.intellij.credentialStore.Credentials;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

import java.net.URI;

public class RobotServiceTest extends BasePlatformTestCase {

    public void testConnect() {
        RobotService service = RobotService.getInstance(getProject());
        assertTrue(service.getRobot().isEmpty());
        Robot robot = service.connect(URI.create("http://localhost:80/"), new Credentials("Default User", "robotics"));
        assertNotEmpty(robot.getSymbols());
        assertTrue(service.getRobot().isPresent());
        assertEquals(robot, service.getRobot().orElseThrow());
    }

}
