package com.bossymr.rapid.robot.ui.log;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class RobotLogPanelView {

    private final JBSplitter splitter;

    private final JPanel listPanel;
    private final ConsoleView consoleView;

    public RobotLogPanelView(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .getConsole();
        JBScrollPane scrollPane = new JBScrollPane();
        scrollPane.add(new JBList<>());
        listPanel = new JPanel();
        listPanel.add(scrollPane);
        splitter = new JBSplitter(false, 0.5f);
        splitter.setFirstComponent(listPanel);
        splitter.setSecondComponent(consoleView.getComponent());
    }

    public @NotNull JPanel getPanel() {
        return splitter;
    }
}
