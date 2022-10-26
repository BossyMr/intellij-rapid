package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RobotService;
import com.intellij.credentialStore.Credentials;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;

public class RobotConnectView extends DialogWrapper {

    private final Project project;

    private JPanel content;
    private JTextField userField;
    private JPasswordField passwordField;
    private JTextField hostField;
    private JTextField portField;
    private ComboBox<AuthenticationType> authenticationComboBox;
    private JPanel hostPanel;

    public RobotConnectView(@NotNull Project project) {
        super(project, false);
        this.project = project;
        setTitle(RapidBundle.message("robot.connect.dialog.title"));

        authenticationComboBox.setRenderer(SimpleListCellRenderer.create((label, value, index) -> label.setText(value.getDisplayName())));
        checkAuthenticationType(authenticationComboBox.getItem());
        authenticationComboBox.addItemListener(e -> checkAuthenticationType((AuthenticationType) e.getItem()));

        init();
    }

    private void checkAuthenticationType(@NotNull AuthenticationType authenticationType) {
        hostPanel.setVisible(authenticationType.equals(AuthenticationType.PASSWORD));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return content;
    }

    @Override
    protected void doOKAction() {
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(new Task.Backgroundable(project, "Connecting") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RobotService service = RobotService.getInstance(project);
                try {
                    URI path = URI.create("http" + "://" + hostField.getText() + ":" + portField.getText());
                    Credentials credentials = authenticationComboBox.getItem().equals(AuthenticationType.DEFAULT) ?
                            new Credentials("Default User", "robotics") :
                            new Credentials(userField.getText(), passwordField.getPassword());
                    try {
                        service.connect(path, credentials);
                    } catch (IOException e) {
                        NotificationGroupManager.getInstance()
                                .getNotificationGroup("Robot Connect Error")
                                .createNotification(RapidBundle.message("notification.title.robot.connect.error", path.toString()), NotificationType.ERROR)
                                .setSubtitle(RapidBundle.message("notification.subtitle.robot.connect.error"))
                                .addAction(new NotificationAction(RapidBundle.message("notification.action.retry.connect")) {
                                    @Override
                                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                                        RobotConnectView connectView = new RobotConnectView(project);
                                        connectView.portField.setText(portField.getText());
                                        connectView.hostField.setText(hostField.getText());
                                        connectView.authenticationComboBox.setItem(authenticationComboBox.getItem());
                                        connectView.userField.setText(userField.getText());
                                        connectView.show();
                                        notification.expire();
                                    }
                                })
                                .notify(project);
                    }
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException(e);
                }
            }
        }, new EmptyProgressIndicator());
        super.doOKAction();
    }

    private void createUIComponents() {
        AuthenticationType[] authenticationTypes = {AuthenticationType.DEFAULT, AuthenticationType.PASSWORD};
        authenticationComboBox = new ComboBox<>(authenticationTypes);
    }
}
