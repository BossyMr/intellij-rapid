package com.bossymr.rapid.robot.impl;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.ui.RobotConnectView;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

public class RobotDelegatingNetworkEngine extends DelegatingNetworkEngine.ShutdownOnFailure {

    private volatile boolean showNotifications = true;

    public RobotDelegatingNetworkEngine(@NotNull NetworkEngine engine) {
        super(engine);
    }

    @Override
    protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
        if (throwable instanceof ResponseStatusException exception) {
            if (exception.getResponse().statusCode() == 400) {
                return;
            }
        }
        RemoteRobotService remoteService = RemoteRobotService.getInstance();
        Robot robot = remoteService.getRobot();
        if (robot != null) {
            if (robot.isConnected()) {
                try {
                    robot.disconnect();
                } catch (IOException | InterruptedException ignored) {}
            }
        }
        if (showNotifications) {
            showNotification(request);
        }
    }

    private void showNotification(@NotNull NetworkCall<?> request) {
        showNotifications = false;
        URI path = request.request().uri();
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Robot Connection Error")
                .createNotification(RapidBundle.message("notification.title.robot.connect.error", path), NotificationType.ERROR)
                .setSubtitle(RapidBundle.message("notification.subtitle.robot.connect.error"))
                .addAction(new ConnectNotificationAction(path))
                .whenExpired(() -> showNotifications = true)
                .notify(null);
    }

    private static class ConnectNotificationAction extends NotificationAction {

        private final @NotNull URI path;

        public ConnectNotificationAction(@NotNull URI path) {
            super(RapidBundle.messagePointer("notification.action.retry.connect"));
            this.path = path;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
            Project project = e.getProject();
            if (project != null) {
                RobotConnectView connectView = new RobotConnectView(project, path);
                connectView.show();
            }
        }
    }
}