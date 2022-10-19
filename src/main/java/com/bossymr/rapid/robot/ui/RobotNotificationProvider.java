package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.codeInsight.intention.PriorityAction;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.LightColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

public class RobotNotificationProvider implements EditorNotificationProvider, DumbAware {

    @Override
    public @NotNull Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(@NotNull Project project, @NotNull VirtualFile file) {
        return fileEditor -> {
            RobotService service = RobotService.getInstance(project);
            if (service.getRobot().isEmpty()) {
                if (file.getFileType().equals(RapidFileType.INSTANCE)) {
                    return new ConnectRobotNotificationPanel(fileEditor);
                }
            }
            return null;
        };
    }

    private static class ConnectRobotNotificationPanel extends EditorNotificationPanel {

        private ConnectRobotNotificationPanel(@NotNull FileEditor fileEditor) {
            super(fileEditor, LightColors.RED);
            setText(RapidBundle.message("editor.notification.no.robot"));
            createActionLabel("Connect", "com.bossymr.rapid.robot.actions.ConnectAction");
        }

        @Override
        protected PriorityAction.@NotNull Priority getIntentionActionPriority() {
            return PriorityAction.Priority.HIGH;
        }
    }
}
