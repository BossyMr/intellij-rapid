package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.RobotState.SymbolState;
import com.bossymr.rapid.robot.network.Controller;
import com.bossymr.rapid.robot.ui.RobotConnectView;
import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static com.intellij.credentialStore.CredentialAttributesKt.generateServiceName;

public final class RobotUtil {

    private RobotUtil() {}

    private static CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(generateServiceName("Robot", key));
    }

    public static @Nullable Credentials getCredentials(@NotNull URI path) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path.toString());
        return PasswordSafe.getInstance().get(credentialAttributes);
    }

    public static void setCredentials(@NotNull URI path, @NotNull Credentials credentials) {
        CredentialAttributes credentialAttributes = createCredentialAttributes(path.toString());
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
    }

    public static void reload() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.INSTANCE, GlobalSearchScope.projectScope(project));
                PsiDocumentManager.getInstance(project).reparseFiles(virtualFiles, true);
            }
        });
    }

    public static @NotNull RobotState getRobotState(@NotNull Controller controller) throws IOException {
        RobotState robotState = new RobotState();
        robotState.name = controller.getName();
        robotState.path = String.valueOf(controller.getPath());
        robotState.symbols = controller.getSymbols();
        return robotState;
    }

    public static @NotNull VirtualSymbol getSymbol(@NotNull SymbolState state) {
        RobotSymbolFactory factory = new RobotSymbolFactory(Collections.singleton(state));
        Map<String, VirtualSymbol> symbols = factory.getSymbols();
        return symbols.get(state.name);
    }

    public static @NotNull Map<String, VirtualSymbol> getSymbols(@NotNull RobotState robotState) {
        RobotSymbolFactory factory = new RobotSymbolFactory(robotState);
        return factory.getSymbols();
    }

    public static void showNotification(@NotNull URI path) {
        showNotification(null, path);
    }

    public static void showNotification(@Nullable Project project, @NotNull URI path) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("Robot Connect Error")
                .createNotification(RapidBundle.message("notification.title.robot.connect.error", path), NotificationType.ERROR)
                .setSubtitle(RapidBundle.message("notification.subtitle.robot.connect.error"))
                .addAction(new NotificationAction(RapidBundle.message("notification.action.retry.connect")) {
                    @Override
                    public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                        RobotConnectView connectView = new RobotConnectView(e.getProject(), path);
                        connectView.show();
                    }
                })
                .notify(project);
    }
}
