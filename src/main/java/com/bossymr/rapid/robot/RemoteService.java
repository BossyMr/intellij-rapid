package com.bossymr.rapid.robot;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

/**
 * A {@code RobotService} is a responsible for communicating with a remote robot. If a robot is currently connected,
 * requests and commands can be transferred and all methods are available. If a robot is persisted, but not connected,
 * only persisted state (modules, routines and symbols) can be retrieved. If a robot is not persisted, no state is
 * available.
 */
public interface RemoteService extends PersistentStateComponent<RemoteService.State>, Disposable {

    @Topic.AppLevel
    Topic<RobotEventListener> TOPIC = Topic.create("Robot Refresh", RobotEventListener.class);

    static @NotNull RemoteService getInstance() {
        return ApplicationManager.getApplication().getService(RemoteService.class);
    }

    /**
     * Returns the robot which is currently persisted, or connected.
     *
     * @return the robot which is currently persisted, or {@code null} if a robot is not persisted.
     */
    @Nullable Robot getRobot();

    /**
     * Connects to the specified path with the specified credentials.
     *
     * @param path the path to connect to.
     * @param credentials the credentials to authenticate with.
     * @return the connected robot.
     * @throws IOException if an I/O error occurs.
     */
    @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException;

    /**
     * Disconnects the currently persisted robot, and deletes all persisted state associated with it.
     *
     * @throws IOException if an I/O error occurs.
     */
    void disconnect() throws IOException;

    @Nullable PersistentRobotState getRobotState();

    void setRobotState(@Nullable PersistentRobotState robotState);

    class State {
        public PersistentRobotState state;
    }
}
