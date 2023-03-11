package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * A {@code StartupActivity} which attempts to connect to a persisted robot.
 */
public class RobotStartupActivity implements StartupActivity.DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        RemoteRobotService service = RemoteRobotService.getInstance();
        service.getRobot().thenComposeAsync(robot -> {
            if (robot != null) {
                if (robot.isConnected()) {
                    return CompletableFuture.completedFuture(null);
                }
                return robot.reconnect();
            }
            return CompletableFuture.completedFuture(null);
        });
    }
}
