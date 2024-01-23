package com.bossymr.rapid.robot.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class RobotToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentFactory contentFactory = ContentFactory.getInstance();
        RobotToolWindow robotToolWindow = RobotToolWindowService.getInstance(project).getToolWindow();
        Content content = contentFactory.createContent(robotToolWindow.getComponent(), null, false);
        content.setDisposer(robotToolWindow);
        toolWindow.getContentManager().addContent(content);
    }
}
