package com.bossymr.rapid.robot;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.api.*;
import com.bossymr.rapid.robot.ui.RobotConnectView;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RobotNetworkAction extends NetworkAction {

    private final Map<String, NetworkQuery<Void>> onClose = new HashMap<>();
    private volatile boolean showNotifications = true;

    public RobotNetworkAction(@NotNull NetworkManager manager) {
        super(manager);
    }

    @Override
    protected <T> boolean onSuccess(@NotNull NetworkTarget<T> target, @Nullable T response) {
        URI previous = target.getPath();
        String path = previous.getPath();
        String query = previous.getQuery();
        if (path != null && path.startsWith("/rw/mastership")) {
            if ("action=request".equals(query)) {
                NetworkTarget<Void> releaseTarget = NetworkTarget.newTarget(RequestMethod.POST, target.getPath(), NetworkType.voidType())
                        .argument("action", "release")
                        .build();
                onClose.put(previous.getPath(), createQuery(releaseTarget));
            }
            if ("action=release".equals(query)) {
                onClose.remove(previous.getPath());
            }
        }
        return true;
    }

    @Override
    protected boolean onFailure(@NotNull NetworkTarget<?> target, @NotNull Throwable throwable) throws IOException, InterruptedException {
        if (throwable instanceof ResponseStatusException exception) {
            if (exception.getResponse().statusCode() == 400) {
                return false;
            }
        }
        RobotService remoteService = RobotService.getInstance();
        RapidRobot robot = remoteService.getRobot();
        if (robot != null) {
            if (robot.isConnected()) {
                try {
                    robot.disconnect();
                } catch (IOException | InterruptedException ignored) {}
            }
        }
        if (showNotifications) {
            showNotification(target);
        }
        close();
        return true;
    }

    @Override
    public void close() throws IOException, InterruptedException {
        for (NetworkQuery<Void> value : onClose.values()) {
            value.get();
        }
        super.close();
    }

    private void showNotification(@NotNull NetworkTarget<?> target) {
        showNotifications = false;
        URI path = getNetworkClient().getBasePath().resolve(target.getPath());
        String presentablePath = getPresentablePath(path);
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Robot connection errors")
                .createNotification(RapidBundle.message("notification.title.robot.connect.error", presentablePath), NotificationType.ERROR)
                .setSubtitle(RapidBundle.message("notification.subtitle.robot.connect.error"))
                .addAction(new ConnectNotificationAction(path))
                .whenExpired(() -> showNotifications = true)
                .notify(null);
    }

    private @NotNull String getPresentablePath(@NotNull URI path) {
        return path.getHost() + (path.getPort() != 80 ? ":" + path.getPort() : "");
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
