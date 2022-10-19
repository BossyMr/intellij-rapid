package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class RobotStartupActivity implements StartupActivity, StartupActivity.DumbAware {

    private static final Logger LOG = Logger.getInstance(RobotStartupActivity.class);

    @Override
    public void runActivity(@NotNull Project project) {
        RobotService service = RobotService.getInstance(project);
        Optional<Robot> robot = service.getRobot();
        if (robot.isPresent()) {
            try {
                robot.get().reconnect();
            } catch (IOException e) {
                LOG.error("Failed to reconnect to persisted robot: " + robot.get().getPath());
            }
        }
    }
}
