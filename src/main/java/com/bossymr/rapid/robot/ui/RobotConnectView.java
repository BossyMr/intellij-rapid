package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.impl.RobotUtil;
import com.bossymr.rapid.robot.network.Controller;
import com.intellij.credentialStore.Credentials;
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

    public RobotConnectView(@Nullable Project project) {
        super(project, false);
        this.project = project;
        setTitle(RapidBundle.message("robot.connect.dialog.title"));

        authenticationComboBox.setRenderer(SimpleListCellRenderer.create((label, value, index) -> label.setText(value.getDisplayName())));
        checkAuthenticationType(authenticationComboBox.getItem());
        authenticationComboBox.addItemListener(e -> checkAuthenticationType((AuthenticationType) e.getItem()));

        init();
    }

    public RobotConnectView(@Nullable Project project, @NotNull URI path) {
        this(project);
        Credentials credentials = RobotUtil.getCredentials(path);
        if (credentials != null) {
            if (!(credentials.equals(Controller.DEFAULT_CREDENTIALS))) {
                authenticationComboBox.setItem(AuthenticationType.PASSWORD);
                userField.setText(credentials.getUserName());
                passwordField.setText(credentials.getPasswordAsString());
            }
        }
        hostField.setText(path.getHost());
        portField.setText(String.valueOf(path.getPort()));
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
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(new Task.Backgroundable(project, RapidBundle.message("robot.connect.progress.indicator.title", hostField.getText())) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RobotService service = RobotService.getInstance();
                URI path = URI.create("http" + "://" + hostField.getText() + ":" + portField.getText());
                Credentials credentials = authenticationComboBox.getItem().equals(AuthenticationType.DEFAULT) ?
                        Controller.DEFAULT_CREDENTIALS :
                        new Credentials(userField.getText(), passwordField.getPassword());
                try {
                    service.connect(path, credentials);
                } catch (IOException e) {
                    RobotUtil.showNotification(path);
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
