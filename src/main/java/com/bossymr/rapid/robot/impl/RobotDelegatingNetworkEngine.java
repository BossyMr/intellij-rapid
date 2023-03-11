package com.bossymr.rapid.robot.impl;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ResponseStatusException;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.robot.RemoteRobotService;
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
import java.net.http.HttpRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class RobotDelegatingNetworkEngine extends DelegatingNetworkEngine {

    private final Map<String, Runnable> onClose = new HashMap<>();
    private volatile boolean showNotifications = true;

    public RobotDelegatingNetworkEngine(@NotNull NetworkEngine engine) {
        super(engine);
    }

    @Override
    protected <T> void onSuccess(@NotNull NetworkCall<T> request, @Nullable T response) {
        URI previous = request.request().uri();
        String path = previous.getPath();
        String query = previous.getQuery();
        if (path != null && path.startsWith("/rw/mastership")) {
            if ("action=request".equals(query)) {
                try {
                    HttpRequest httpRequest = HttpRequest.newBuilder(request.request(), (k, v) -> true)
                            .uri(new URI(previous.getScheme(), previous.getUserInfo(), previous.getHost(), previous.getPort(), previous.getPath(), "action=release", previous.getFragment()))
                            .build();
                    onClose.put(previous.getPath(), () -> getNetworkClient().sendAsync(httpRequest));
                } catch (URISyntaxException ignored) {}
            }
            if ("action=release".equals(query)) {
                onClose.remove(previous.getPath());
            }
        }
    }

    @Override
    protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
        while (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }
        if (throwable instanceof ResponseStatusException exception) {
            if (exception.getResponse().statusCode() == 400) {
                return;
            }
        }
        RemoteRobotService remoteService = RemoteRobotService.getInstance();
        remoteService.getRobot().thenComposeAsync(robot -> {
            if (robot != null) {
                return robot.disconnect();
            } else {
                return CompletableFuture.completedFuture(null);
            }
        });
        if (showNotifications) {
            showNotification(request, throwable);
        }
    }

    @Override
    public void close() throws IOException, InterruptedException {
        onClose.values().forEach(Runnable::run);
        super.close();
    }

    private void showNotification(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
        showNotifications = false;
        URI path = request.request().uri();
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Robot Connection Error")
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