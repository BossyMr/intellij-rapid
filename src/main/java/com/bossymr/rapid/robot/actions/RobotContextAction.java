package com.bossymr.rapid.robot.actions;

import com.bossymr.rapid.robot.ui.RobotToolWindow;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class RobotContextAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void update(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Presentation presentation = event.getPresentation();
        if (project == null) {
            presentation.setEnabledAndVisible(false);
            return;
        }
        if (Objects.equals(event.getPlace(), RobotToolWindow.ROBOT_TOOL_WINDOW_GROUP)) {
            presentation.setEnabledAndVisible(isAvailable(event));
        }
    }

    private boolean isAvailable(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        if (project == null) {
            return false;
        }
        Object[] selectedItems = PlatformCoreDataKeys.SELECTED_ITEMS.getData(event.getDataContext());
        if (selectedItems != null) {
            for (Object selectedItem : selectedItems) {
                if (isAvailable(event, selectedItem)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected abstract boolean isAvailable(@NotNull AnActionEvent event, @NotNull Object selected);

}
