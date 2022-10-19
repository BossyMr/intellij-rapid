package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DisconnectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        RobotService service = RobotService.getInstance(project);
        try {
            service.getRobot().orElseThrow().disconnect();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null &&
                RobotService.getInstance(project).getRobot().isPresent() &&
                RobotService.getInstance(project).getRobot().orElseThrow().isConnected());
    }
}
