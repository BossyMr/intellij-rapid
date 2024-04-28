package com.bossymr.rapid.robot.ui.log;

import com.bossymr.rapid.robot.network.EventLogMessage;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.List;

public class RobotLogPanelView {

    private final JPanel panel;

    private final RobotLogListPanel list;

    private final SearchTextField field;

    private final ConsoleView consoleView;

    public RobotLogPanelView(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        consoleView = createConsoleView(project, toolWindow);

        list = new RobotLogListPanel();

        field = new SearchTextField(false);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.add(field, BorderLayout.NORTH);
        listPanel.add(new JBScrollPane(list), BorderLayout.CENTER);

        field.addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                List<EventLogMessage> messages = RobotLogModel.getInstance().getMessages().stream()
                        .filter(event -> {
                            String messageTitle = event.getMessageTitle();
                            return messageTitle != null && messageTitle.contains(field.getText());
                        })
                        .toList();
                list.getModel().clear();
                list.getModel().addAll(messages);
            }
        });

        JBSplitter splitter = new JBSplitter(false, 0.5f);
        splitter.setFirstComponent(listPanel);
        splitter.setSecondComponent(consoleView.getComponent());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar("RobotPanel", createActionGroup(), false);
        toolbar.setTargetComponent(splitter);

        panel = new JPanel(new BorderLayout());
        panel.add(toolbar.getComponent(), BorderLayout.WEST);
        panel.add(splitter, BorderLayout.CENTER);

        // Update the console view to reflect the selected event
        list.addListSelectionListener(event -> {
            EventLogMessage selectedValue = list.getSelectedValue();
            consoleView.clear();
            if (selectedValue != null) {
                writeMessage(selectedValue);
            }
        });

        // Add a new event to the list
        RobotLogModel.getInstance().onMessage(event -> ApplicationManager.getApplication().invokeLater(() -> {
            String messageTitle = event.getMessageTitle();
            if (messageTitle == null || !(messageTitle.contains(field.getText()))) {
                return;
            }
            list.getModel().addElement(event);
            if (RobotLogModel.getInstance().isAutoScroll()) {
                list.setSelectedIndex(list.getModel().getSize() - 1);
            }
        }, ModalityState.any()));

        RobotLogModel.getInstance().onRefresh(reload -> {
            if (reload) {
                list.getModel().clear();
                list.getModel().addAll(RobotLogModel.getInstance().getMessages());
            }
            if (RobotLogModel.getInstance().isAutoScroll()) {
                if (!(list.getModel().isEmpty())) {
                    list.setSelectedIndex(list.getModel().getSize() - 1);
                }
            }
        });
    }

    private void writeMessage(@NotNull EventLogMessage message) {
        consoleView.print(message.getMessageCode() + " [" + message.getTimestamp() + "] " + message.getMessageTitle(), ConsoleViewContentType.NORMAL_OUTPUT);
        if (!StringUtil.isEmptyOrSpaces(message.getDescription())) {
            consoleView.print("\n" + message.getDescription(), ConsoleViewContentType.NORMAL_OUTPUT);
        }
        if (!StringUtil.isEmptyOrSpaces(message.getActions())) {
            consoleView.print("\nActions: " + message.getActions(), ConsoleViewContentType.NORMAL_OUTPUT);
        }
        if (!StringUtil.isEmptyOrSpaces(message.getCauses())) {
            consoleView.print("\nCauses: " + message.getCauses(), ConsoleViewContentType.NORMAL_OUTPUT);
        }
        if (!StringUtil.isEmptyOrSpaces(message.getConsequences())) {
            consoleView.print("\nConsequences: " + message.getConsequences(), ConsoleViewContentType.NORMAL_OUTPUT);
        }
        consoleView.scrollTo(0);
    }

    private @NotNull ConsoleView createConsoleView(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance()
                .createBuilder(project)
                .getConsole();
        Disposer.register(toolWindow.getDisposable(), consoleView);
        consoleView.clear();
        consoleView.allowHeavyFilters();
        return consoleView;
    }

    public @NotNull JPanel getPanel() {
        return panel;
    }

    private @NotNull ActionGroup createActionGroup() {
        RobotLogModel model = RobotLogModel.getInstance();
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(model.new FilterTypeAction(RobotLogModel.EventType.ERROR));
        actionGroup.add(model.new FilterTypeAction(RobotLogModel.EventType.WARNING));
        actionGroup.add(model.new FilterTypeAction(RobotLogModel.EventType.INFORMATION));
        actionGroup.addSeparator();
        actionGroup.add(model.new AutoScrollAction());
        actionGroup.addSeparator();
        actionGroup.add(model.new ClearLogAction());
        return actionGroup;
    }
}
