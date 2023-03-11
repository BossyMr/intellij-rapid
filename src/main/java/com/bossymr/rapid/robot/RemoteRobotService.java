package com.bossymr.rapid.robot;

import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.symbol.RapidRobot;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code RobotService} is a responsible for communicating with a remote robot. If a robot is currently connected,
 * requests and commands can be transferred and all methods are available. If a robot is persisted, but not connected,
 * only persisted state (modules, routines and symbols) can be retrieved. If a robot is not persisted, no state is
 * available.
 */
public interface RemoteRobotService extends PersistentStateComponent<RemoteRobotService.State>, Disposable {

    @Topic.AppLevel
    Topic<RobotEventListener> TOPIC = Topic.create("Robot Refresh", RobotEventListener.class);

    static @NotNull RemoteRobotService getInstance() {
        return ApplicationManager.getApplication().getService(RemoteRobotService.class);
    }

    static boolean isConnected() {
        RemoteRobotService service = RemoteRobotService.getInstance();
        RapidRobot robot = service.getRobot().getNow(null);
        if (robot != null) {
            return robot.isConnected();
        }
        return false;
    }

    /**
     * Returns the robot which is currently persisted, or connected.
     *
     * @return the robot which is currently persisted, or {@code null} if a robot is not persisted.
     */
    @NotNull CompletableFuture<@Nullable RapidRobot> getRobot();

    /**
     * Connects to the specified path with the specified credentials.
     *
     * @param path the path to connect to.
     * @param credentials the credentials to authenticate with.
     * @return the connected robot.
     */
    @NotNull CompletableFuture<@NotNull RapidRobot> connect(@NotNull URI path, @NotNull Credentials credentials);

    /**
     * Disconnects the currently persisted robot, and deletes all persisted state associated with it.
     */
    CompletableFuture<Void> disconnect();

    @Nullable RapidRobot.State getRobotState();

    void setRobotState(@Nullable RapidRobot.State robotState);

    class State {
        public RapidRobot.State state;
    }
}
