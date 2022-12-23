package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RemoteService;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class RobotConnectView extends DialogWrapper {

    private final Project project;

    private final JPanel content;
    private final JTextField userField;
    private final JPasswordField passwordField;
    private final JTextField hostField;
    private final JTextField portField;
    private final ComboBox<AuthenticationType> authenticationComboBox;
    private final JPanel userPanel;

    public RobotConnectView(@Nullable Project project) {
        super(project, false);
        this.project = project;
        setTitle(RapidBundle.message("robot.connect.dialog.title"));

        content = new JPanel(new GridBagLayout());
        hostField = new JTextField();
        portField = new JTextField();
        AuthenticationType[] authenticationTypes = {AuthenticationType.DEFAULT, AuthenticationType.PASSWORD};
        authenticationComboBox = new ComboBox<>(authenticationTypes);

        userPanel = new JPanel(new GridBagLayout());
        userField = new JTextField();
        passwordField = new JBPasswordField();

        content.add(LabeledComponent.create(hostField, RapidBundle.message("robot.connect.host.label"), BorderLayout.WEST), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));
        content.add(LabeledComponent.create(portField, RapidBundle.message("robot.connect.host.port"), BorderLayout.WEST), new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));

        content.add(LabeledComponent.create(authenticationComboBox, RapidBundle.message("robot.connect.authentication.type"), BorderLayout.WEST), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));

        userPanel.add(LabeledComponent.create(authenticationComboBox, RapidBundle.message("robot.connect.authentication.username"), BorderLayout.WEST), new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));
        userPanel.add(LabeledComponent.create(authenticationComboBox, RapidBundle.message("robot.connect.authentication.password"), BorderLayout.WEST), new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));

        content.add(userPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, JBUI.emptyInsets(), 0, 0));

        authenticationComboBox.setRenderer(SimpleListCellRenderer.create((label, value, index) -> label.setText(value.getDisplayName())));
        checkAuthenticationType(authenticationComboBox.getItem());
        authenticationComboBox.addItemListener(e -> checkAuthenticationType((AuthenticationType) e.getItem()));

        init();
    }

    private void checkAuthenticationType(@NotNull AuthenticationType authenticationType) {
        userPanel.setVisible(authenticationType.equals(AuthenticationType.PASSWORD));
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return content;
    }

    @Override
    protected void doOKAction() {
        Task.Backgroundable task = new Task.Backgroundable(project, RapidBundle.message("robot.connect.progress.indicator.title", hostField.getText())) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RemoteService service = RemoteService.getInstance();
                URI path = URI.create("http" + "://" + hostField.getText() + ":" + portField.getText());
                Credentials credentials = authenticationComboBox.getItem().equals(AuthenticationType.DEFAULT) ? new Credentials("Default User", "robotics".toCharArray()) :
                        new Credentials(userField.getText(), passwordField.getPassword());
                try {
                    service.connect(path, credentials);
                } catch (IOException | InterruptedException ignored) {}
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        super.doOKAction();
    }
}
