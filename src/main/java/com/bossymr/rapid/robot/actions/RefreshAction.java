package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RefreshAction extends AnAction {
    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabled(project != null && RemoteRobotService.getInstance().getRobot().getNow(null) != null);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        new Task.Backgroundable(project, RapidBundle.message("robot.refresh.action")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RemoteRobotService service = RemoteRobotService.getInstance();
                CompletableFuture<@Nullable RapidRobot> completableFuture = service.getRobot();
                try {
                    completableFuture.thenComposeAsync(robot -> {
                        if (robot != null) {
                            return robot.reconnect();
                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    }).get();
                } catch (InterruptedException | ExecutionException ignored) {}
            }
        }.queue();
    }
}
