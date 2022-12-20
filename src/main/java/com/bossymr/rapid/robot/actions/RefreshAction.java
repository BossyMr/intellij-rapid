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

public class RefreshAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        Task.Backgroundable task = new Task.Backgroundable(project, "Refreshing...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RemoteService service = RemoteService.getInstance();
                Robot robot = service.getRobot();
                assert robot != null;
                try {
                    robot.reconnect();
                } catch (IOException | InterruptedException ignored) {}
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
        e.getPresentation().setEnabled(project != null &&
                RemoteService.getInstance().getRobot() != null);
    }
}
