package com.bossymr.rapid.robot.ui.log;

import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.api.NetworkManager;
import com.bossymr.rapid.robot.api.SubscriptionPriority;
import com.bossymr.rapid.robot.network.EventLogCategory;
import com.bossymr.rapid.robot.network.EventLogMessage;
import com.bossymr.rapid.robot.network.EventLogService;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.util.List;

public class RobotLogPanelView {

    private final JBSplitter splitter;

    private final RobotLogListPanel list;
    private final ConsoleView consoleView;

    public RobotLogPanelView(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        consoleView = TextConsoleBuilderFactory.getInstance()
                                               .createBuilder(project)
                                               .getConsole();
        Disposer.register(toolWindow.getDisposable(), consoleView);
        JBScrollPane scrollPane = new JBScrollPane();
        list = new RobotLogListPanel();
        scrollPane.add(list);
        JPanel panel = new JPanel();
        panel.add(scrollPane);
        splitter = new JBSplitter(false, 0.5f);
        splitter.setFirstComponent(list);
        splitter.setSecondComponent(consoleView.getComponent());

        list.addListSelectionListener(event -> {
            int firstIndex = event.getFirstIndex();
            consoleView.clear();
            if (firstIndex >= 0) {
                EventLogMessage element = list.getModel().getElementAt(firstIndex);
                consoleView.print("[" + element.getTimestamp() + "] " + element.getMessageTitle(), ConsoleViewContentType.NORMAL_OUTPUT);
            }
        });

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            RapidRobot robot = RobotService.getInstance().getRobot();
            if (robot == null) {
                return null;
            }
            NetworkManager manager = robot.getNetworkManager();
            if (manager == null) {
                manager = robot.reconnect();
            }
            // TODO: This should not be managed by this class
            // TODO: Allow changing language code
            EventLogService service = manager.createService(EventLogService.class);
            List<EventLogCategory> categories = service.getCategories("en").get();
            for (EventLogCategory category : categories) {
                category.onMessage().subscribe(SubscriptionPriority.MEDIUM, (entity, event) -> {
                    try {
                        EventLogMessage message = event.getMessage("en").get();
                        onEvent(message);
                    } catch (IOException | InterruptedException ignored) {}
                });
            }
            return null;
        });
    }

    private void onEvent(@NotNull EventLogMessage message) {
        list.getModel().addElement(message);
    }

    public @NotNull JPanel getPanel() {
        return splitter;
    }
}
