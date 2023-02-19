package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.language.symbol.RapidModule;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class DeleteAction extends AnAction {
    private static boolean isAvailable(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return false;
        }
        Object[] selectedItems = PlatformCoreDataKeys.SELECTED_ITEMS.getData(event.getDataContext());
        if (selectedItems != null) {
            for (Object selectedItem : selectedItems) {
                if (selectedItem instanceof RapidRobot || selectedItem instanceof RapidModule) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        RemoteRobotService service = RemoteRobotService.getInstance();
        try {
            service.disconnect();
        } catch (IOException | InterruptedException ignored) {
        }
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project != null) {
            RemoteRobotService service = RemoteRobotService.getInstance();
            RapidRobot robot = service.getRobot();
            event.getPresentation().setEnabled(robot != null && isAvailable(event));
        } else {
            event.getPresentation().setEnabledAndVisible(false);
        }
    }
}
