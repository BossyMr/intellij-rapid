package com.bossymr.rapid.robot;

import com.bossymr.rapid.RapidTestCase;
import com.bossymr.rapid.RobotTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@RobotTest
class RapidRobotTest extends RapidTestCase {

    @Test
    void connectToRobot() throws IOException, InterruptedException {
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
