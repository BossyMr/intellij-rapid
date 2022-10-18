package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class RobotStartupActivity implements StartupActivity, StartupActivity.DumbAware {
    @Override
    public void runActivity(@NotNull Project project) {
        RobotService service = RobotService.getInstance(project);
        service.getRobot();
    }
}
