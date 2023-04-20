package com.bossymr.rapid.robot.impl;

import com.bossymr.network.NetworkManager;
import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.RapidFileType;
import com.bossymr.rapid.robot.RapidRobot;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.RobotEventListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

@State(name = "robot",
        storages = {
                @Storage("robot.xml")
        })
public class RemoteRobotServiceImpl implements RemoteRobotService {

    private @NotNull State state = new State();
    private @Nullable RapidRobot robot;

    @Override
    public @Nullable RapidRobot getRobot() {
        if (robot != null) {
            return robot;
        }
        RapidRobot.State state = getRobotState();
        if (state != null) {
            RapidRobot value = RapidRobot.create(state);
            registerRobot(value);
            this.robot = value;
            reload();
            return robot;
        }
        return null;
    }

    private void registerRobot(@NotNull RapidRobot robot) {
        Disposer.register(this, robot);
        ApplicationManager.getApplication().getMessageBus().connect(robot).subscribe(RapidRobot.STATE_TOPIC, (RapidRobot.StateListener) (result, state) -> {
            if (result == robot) {
                setRobotState(state);
            }
        });
    }

    @Override
    public @NotNull RapidRobot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        if (robot != null) {
            robot.disconnect();
            Disposer.dispose(robot);
        }
        RapidRobot robot = RapidRobot.connect(path, credentials);
        setRobotState(robot.getState());
        registerRobot(robot);
        reload();
        NetworkManager manager = robot.getNetworkManager();
        Objects.requireNonNull(manager);
        RobotEventListener.publish().onConnect(robot, manager);
        return this.robot = robot;
    }

    @Override
    public void disconnect() throws IOException, InterruptedException {
        if (robot == null) {
            return;
        }
        robot.disconnect();
        Disposer.dispose(robot);
        RobotEventListener.publish().onRemoval(robot);
        Path path = Path.of(PathManager.getSystemPath(), "robot");
        WriteAction.runAndWait(() -> {
            VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByNioFile(path);
            if (virtualFile != null) {
                for (Project project : ProjectManager.getInstance().getOpenProjects()) {
                    PsiDirectory directory = PsiManager.getInstance(project).findDirectory(virtualFile);
                    if (directory != null) {
                        directory.delete();
                    }
                }
            }
            FileUtil.delete(path);
        });
        robot = null;
        setRobotState(null);
        reload();
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
