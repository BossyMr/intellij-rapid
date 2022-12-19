package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.RemoteService;
import com.bossymr.rapid.robot.Robot;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RobotStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        RemoteService service = RemoteService.getInstance();
        Robot robot = service.getRobot();
        if (robot != null) {
            if (robot.getRobotService() == null) {
                try {
                    robot.reconnect();
                } catch (IOException ignored) {}
            }
        }
    }
}
