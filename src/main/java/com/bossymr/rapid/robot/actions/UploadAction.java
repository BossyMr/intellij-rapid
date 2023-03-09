package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.impl.RobotUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class UploadAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        new Task.Backgroundable(project, RapidBundle.message("robot.upload.action")) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RemoteRobotService service = RemoteRobotService.getInstance();
                RapidRobot robot = service.getRobot();
                assert robot != null;
                try {
                    robot.upload().get();
                } catch (ExecutionException | InterruptedException ignored) {}
            }
        }.queue();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(RobotUtil.isConnected(e.getProject()));
    }
}
