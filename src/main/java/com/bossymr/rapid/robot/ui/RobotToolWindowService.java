package com.bossymr.rapid.robot.ui;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RobotToolWindowService {

    private final Project project;
    private RobotToolWindow window;

    public RobotToolWindowService(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull RobotToolWindowService getInstance(@NotNull Project project) {
        return project.getService(RobotToolWindowService.class);
    }

    public @NotNull RobotToolWindow getToolWindow() {
        if (window == null) {
            window = new RobotToolWindow(project);
        }
        return window;
    }

}
