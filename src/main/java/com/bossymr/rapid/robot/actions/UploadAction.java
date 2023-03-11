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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UploadAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Objects.requireNonNull(project);
        new Task.Backgroundable(project, RapidBundle.message("robot.upload.action")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RemoteRobotService service = RemoteRobotService.getInstance();
                CompletableFuture<@Nullable RapidRobot> completableFuture = service.getRobot();
                try {
                    completableFuture.thenComposeAsync(robot -> {
                        if (robot != null) {
                            return robot.upload();
                        } else {
                            return CompletableFuture.completedFuture(null);
                        }
                    }).get();
                } catch (InterruptedException | ExecutionException ignored) {}
            }
        }.queue();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(e.getProject() != null && RemoteRobotService.isConnected());
    }
}
