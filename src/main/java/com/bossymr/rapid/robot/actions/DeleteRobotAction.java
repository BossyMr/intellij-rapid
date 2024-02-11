package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DeleteRobotAction extends RobotContextAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        new Task.Backgroundable(project, RapidBundle.message("robot.delete.action")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RobotService service = RobotService.getInstance();
                try {
                    service.disconnect();
                } catch (IOException | InterruptedException ignored) {}
            }
        }.queue();
    }

    @Override
    protected boolean isAvailable(@NotNull AnActionEvent event, @NotNull Object selected) {
        return selected instanceof RapidRobot;
    }
}
