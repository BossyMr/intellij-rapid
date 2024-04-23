package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.RapidIcons;
import com.bossymr.rapid.robot.ui.log.RobotLogPanelView;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

public class RobotLogToolWindowFactory implements ToolWindowFactory, DumbAware {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        toolWindow.setTitle(RapidBundle.message("robot.tool.window.title"));
        toolWindow.setIcon(RapidIcons.ROBOT_TOOL_WINDOW);
        RobotLogPanelView logView = new RobotLogPanelView(project, toolWindow);
        Content content = contentManager.getFactory().createContent(logView.getPanel(), RapidBundle.message("robot.tool.window.tab.log"), true);
        content.setCloseable(false);
        contentManager.addContent(content);
    }
}
