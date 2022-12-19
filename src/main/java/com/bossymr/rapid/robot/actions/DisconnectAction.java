package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.robot.RemoteService;
import com.bossymr.rapid.robot.Robot;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DisconnectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        Task.Backgroundable task = new Task.Backgroundable(project, "Disconnecting...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RemoteService service = RemoteService.getInstance();
                Robot robot = service.getRobot();
                assert robot != null;
                try {
                    robot.disconnect();
                } catch (IOException ignored) {}
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));

    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            RemoteService service = RemoteService.getInstance();
            Robot robot = service.getRobot();
            if (robot != null) {
                if (robot.getRobotService() != null) {
                    e.getPresentation().setEnabled(true);
                    return;
                }
            }
        }
        e.getPresentation().setEnabled(false);
    }
}
