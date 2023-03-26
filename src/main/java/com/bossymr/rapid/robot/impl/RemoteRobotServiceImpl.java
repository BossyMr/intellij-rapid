package com.bossymr.rapid.robot.impl;

import com.bossymr.network.client.NetworkEngine;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.RobotEventListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@State(name = "robot",
        storages = {
                @Storage("robot.xml")
        })
public class RemoteRobotServiceImpl implements RemoteRobotService {

    private @NotNull State state = new State();
    private @Nullable CompletableFuture<RapidRobot> robot;

    private @Nullable MessageBusConnection connection;

    @Override
    public @NotNull CompletableFuture<@Nullable RapidRobot> getRobot() {
        if (robot != null) {
            return robot;
        }
        RapidRobot.State state = getRobotState();
        if (state != null) {
            RapidRobot value = RapidRobot.create(state);
            registerRobot(value);
            robot = CompletableFuture.completedFuture(value);
            reload();
            return robot;
        }
        return CompletableFuture.completedFuture(null);
    }

    private void registerRobot(@NotNull RapidRobot robot) {
        if (connection != null) {
            connection.disconnect();
        }
        // FIXME: 2023-03-22 Connection already disposed?
        connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(RapidRobot.STATE_TOPIC, (RapidRobot.StateListener) (result, state) -> {
            if (result == robot) {
                setRobotState(state);
            }
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull RapidRobot> connect(@NotNull URI path, @NotNull Credentials credentials) {
        CompletableFuture<?> completableFuture;
        if (robot != null) {
            completableFuture = robot.thenComposeAsync(RapidRobot::disconnect);
        } else {
            completableFuture = CompletableFuture.completedFuture(null);
        }
        return robot = completableFuture
                .thenComposeAsync(unused -> RapidRobot.connect(path, credentials))
                .thenApplyAsync(robot -> {
                    setRobotState(robot.getState());
                    registerRobot(robot);
                    reload();
                    NetworkEngine engine = robot.getNetworkEngine();
                    if (engine != null) {
                        RobotEventListener.publish().onConnect(robot, engine);
                    }
                    return robot;
                });
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnect() {
        if (robot == null) {
            return CompletableFuture.completedFuture(null);
        }
        CompletableFuture<Void> completableFuture = robot.thenComposeAsync(result -> result.disconnect().thenRunAsync(() -> {
            RobotEventListener.publish().onRemoval(result);
            Path path = Path.of(PathManager.getSystemPath(), "robot");
            WriteAction.runAndWait(() -> {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(path);
                if (virtualFile != null) {
                    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                        PsiDirectory directory = PsiManager.getInstance(project).findDirectory(virtualFile);
                        if(directory != null) {
                            directory.delete();
                        }
                    }
                    reload();
                }
            });
        }));
        robot = null;
        setRobotState(null);
        reload();
        return completableFuture;
    }

    private void reload() {
        ApplicationManager.getApplication().invokeLater(() -> {
            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {
                Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(RapidFileType.getInstance(), GlobalSearchScope.projectScope(project));
                PsiDocumentManager.getInstance(project).reparseFiles(virtualFiles, true);
            }
        });
    }

    @Override
    public void dispose() {}

    @Override
    public @Nullable RapidRobot.State getRobotState() {
        return getState().state;
    }

    @Override
    public void setRobotState(@Nullable RapidRobot.State robotState) {
        getState().state = robotState;
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
