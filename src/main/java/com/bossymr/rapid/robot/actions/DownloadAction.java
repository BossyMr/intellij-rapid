package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DownloadAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        new Task.Backgroundable(project, RapidBundle.message("robot.download.action")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RobotService service = RobotService.getInstance();
                RapidRobot robot = service.getRobot();
                if (robot != null) {
                    try {
                        robot.download();
                    } catch (IOException | InterruptedException ignored) {}
                }
            }
        }.queue();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && RobotService.isConnected());
    }
}
