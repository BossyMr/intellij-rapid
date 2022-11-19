package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RobotStartupActivity implements StartupActivity, StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        RobotService service = RobotService.getInstance();
        Robot robot = service.getRobot();
        if (robot != null) {
            try {
                robot.reconnect();
            } catch (IOException e) {
                RobotUtil.showNotification(robot.getPath());
            }
        }
    }
}
