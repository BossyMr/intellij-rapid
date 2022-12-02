package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.impl.RobotUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DeleteAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        RobotService service = RobotService.getInstance();
        try {
            service.disconnect();
        } catch (IOException ex) {
            RobotUtil.showNotification();
        }
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project != null) {
            RobotService service = RobotService.getInstance();
            Robot robot = service.getRobot();
            e.getPresentation().setEnabledAndVisible(robot != null);
        } else {
            e.getPresentation().setEnabledAndVisible(false);
        }
    }
}
