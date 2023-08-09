package com.bossymr.rapid.robot;

import com.bossymr.network.GenericType;
import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.NetworkRequest;
import com.bossymr.rapid.RapidBundle;
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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class RobotNetworkAction extends NetworkAction {

    private final Map<String, NetworkRequest<Void>> onClose = new HashMap<>();
    private volatile boolean showNotifications = true;

    public RobotNetworkAction(@NotNull NetworkManager manager) {
        super(manager);
    }

    @Override
    protected <T> boolean onSuccess(@NotNull NetworkRequest<T> request, @Nullable T response) {
        URI previous = request.getPath();
        String path = previous.getPath();
        String query = previous.getQuery();
        if (path != null && path.startsWith("/rw/mastership")) {
            if ("action=request".equals(query)) {
                try {
                    URI queryPath = new URI(previous.getScheme(), previous.getUserInfo(), previous.getHost(), previous.getPort(), previous.getPath(), "action=release", previous.getFragment());
                    NetworkRequest<Void> networkRequest = new NetworkRequest<>(queryPath, GenericType.of(Void.class));
                    networkRequest.getFields().putAll(request.getFields());
                    onClose.put(previous.getPath(), networkRequest);
                } catch (URISyntaxException ignored) {}
            }
            if ("action=release".equals(query)) {
                onClose.remove(previous.getPath());
            }
        }
        return true;
    }

    @Override
    protected boolean onFailure(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) {
        if (throwable instanceof ResponseStatusException exception) {
            if (exception.getResponse().code() == 400) {
                return true;
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
            showNotification(request, throwable);
        }
        return true;
    }

    @Override
    public void close() throws IOException, InterruptedException {
        for (NetworkRequest<Void> value : onClose.values()) {
            getNetworkClient().send(value).close();
        }
        super.close();
    }

    private void showNotification(@NotNull NetworkRequest<?> request, @NotNull Throwable throwable) {
        showNotifications = false;
        URI path = request.getPath();
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Robot connection errors")
                .createNotification(RapidBundle.message("notification.title.robot.connect.error", path), NotificationType.ERROR)
                .setContent(throwable.getLocalizedMessage())
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
