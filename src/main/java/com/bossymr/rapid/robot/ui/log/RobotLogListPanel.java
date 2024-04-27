package com.bossymr.rapid.robot.ui.log;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.network.EventLogMessage;
import com.intellij.icons.AllIcons;
import com.intellij.ide.CopyProvider;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBList;
import com.intellij.util.ui.JBInsets;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RobotLogListPanel extends JBList<EventLogMessage> implements DataProvider, CopyProvider {

    public RobotLogListPanel() {
        super(new ArrayList<>());
        setCellRenderer(new RobotLogListCellRenderer());
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setEmptyText(RapidBundle.message("robot.tool.window.tab.log.empty.text"));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public DefaultListModel<EventLogMessage> getModel() {
        return (DefaultListModel<EventLogMessage>) super.getModel();
    }

    @Override
    public void performCopy(@NotNull DataContext dataContext) {
        if (!isSelectionEmpty()) {
            String copyText = getSelectedValuesList().stream()
                                                     .map(this::getText)
                                                     .collect(Collectors.joining("\n"));
            CopyPasteManager.getInstance().setContents(new StringSelection(copyText));
        }
    }

    private @NotNull String getText(@NotNull EventLogMessage message) {
        return message.getMessageType() + " [" + message.getTimestamp() + "]: " + message.getMessageTitle();
    }

    @Override
    public boolean isCopyEnabled(@NotNull DataContext dataContext) {
        return !isSelectionEmpty();
    }

    @Override
    public boolean isCopyVisible(@NotNull DataContext dataContext) {
        return !isSelectionEmpty();
    }

    @Override
    public @Nullable Object getData(@NotNull @NonNls String dataId) {
        if (PlatformDataKeys.COPY_PROVIDER.is(dataId)) {
            return this;
        }
        return null;
    }

    public static class RobotLogListCellRenderer extends ColoredListCellRenderer<EventLogMessage> {

        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends EventLogMessage> list, @Nullable EventLogMessage value, int index, boolean selected, boolean hasFocus) {
            if (value == null) {
                return;
            }
            setIcon(getIcon(value));
            String messageTitle = value.getMessageTitle();
            if (messageTitle != null) {
                append(messageTitle, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
            setBorderInsets(JBInsets.create(5, 5));
        }

        private @NotNull Icon getIcon(@NotNull EventLogMessage message) {
            return switch (message.getMessageType()) {
                case INFORMATION -> AllIcons.General.Information;
                case WARNING -> AllIcons.General.Warning;
                case ERROR -> AllIcons.General.Error;
            };
        }
    }
}
