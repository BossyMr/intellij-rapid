package com.bossymr.rapid.robot.ui;

import com.bossymr.rapid.robot.RobotService;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;

public class RobotConnect extends DialogWrapper {

    private static final Logger LOG = Logger.getInstance(RobotConnect.class);

    private final Project project;

    private JPanel contentPane;
    private JTextField hostField;
    private JTextField userField;
    private JPasswordField passwordField;

    public RobotConnect(@Nullable Project project) {
        super(project, false);
        this.project = project;
        setTitle("New Robot");
        init();
    }

    private @NotNull URI getHost() {
        return URI.create(hostField.getText());
    }

    private @NotNull Credentials getCredentials() {
        return new Credentials(userField.getText(), passwordField.getPassword());
    }

    @Override
    protected void doOKAction() {
        Task.Backgroundable task = new Task.Backgroundable(project, "Connecting...") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                RobotService service = RobotService.getInstance(project);
                try {
                    service.connect(getHost(), getCredentials());
                } catch (IOException e) {
                    LOG.error("Could not connect to path: " + hostField.getText(), e);
                }
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
        super.doOKAction();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
