package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * A {@code StartupActivity} which attempts to connect to a persisted robot.
 */
public class RobotStartupActivity implements ProjectActivity {

    private static final Logger logger = Logger.getInstance(RobotStartupActivity.class);

    @SuppressWarnings("resource")
    @Override
    public @Nullable Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        RobotService service = RobotService.getInstance();
        RapidRobot robot = service.getRobot();
        if (robot != null) {
            if (robot.isConnected()) {
                return null;
            }
            try {
                robot.reconnect();
            } catch (IOException ignored) {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return null;
    }
}
