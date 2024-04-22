package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.ui.RobotConnectView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConnectRobotAction extends RobotContextAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Objects.requireNonNull(project);
        RobotConnectView connectView = new RobotConnectView(project);
        connectView.show();
    }

    @Override
    protected boolean isAvailable(@NotNull AnActionEvent event, @NotNull Object selected) {
        return selected instanceof RapidRobot;
    }
}
