package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.ui.RobotConnectView;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        assert project != null;
        new RobotConnectView(project).show();
    }

    private static boolean isAvailable(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return false;
        }
        Object[] selectedItems = PlatformCoreDataKeys.SELECTED_ITEMS.getData(event.getDataContext());
        if (selectedItems != null) {
            for (Object selectedItem : selectedItems) {
                if (selectedItem instanceof RapidRobot) {
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
    public void update(@NotNull AnActionEvent event) {
        Presentation presentation = event.getPresentation();
        if (ActionPlaces.isPopupPlace(event.getPlace())) {
            presentation.setEnabledAndVisible(isAvailable(event));
        }
    }

}
