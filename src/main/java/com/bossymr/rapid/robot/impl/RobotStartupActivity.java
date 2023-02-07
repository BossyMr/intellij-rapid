package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RobotStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        RemoteRobotService service = RemoteRobotService.getInstance();
        Robot robot = service.getRobot();
        if (robot != null) {
            if (robot.getNetworkEngine() == null) {
                try {
                    robot.reconnect();
                } catch (IOException | InterruptedException ignored) {
                }
            }
        }
    }
}
