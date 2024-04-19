package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.api.client.security.Credentials;
import com.bossymr.rapid.robot.impl.RobotServiceImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

/**
 * A {@code RobotService} is a responsible for communicating with a remote {@link RapidRobot robot}.
 * <p>
 * This service persists the state of a robot. As such, symbols and modules found on the robot can still be retrieved
 * after the robot has been disconnected. If a robot is currently persisted, it can be retrieved using
 * {@link #getRobot()}. However, this does not mean that this plugin is connected to the robot.
 * <p>
 * In order to disconnect from a robot but keep it persisted, {@link RapidRobot#disconnect()} should be called. In
 * order to disconnect from a robot and delete all persisted state, {@link #disconnect()} should be called.
 */
public interface RobotService extends PersistentStateComponent<RobotService.State>, Disposable {

    @NotNull Credentials DEFAULT_CREDENTIALS = new Credentials("Default User", "robotics".toCharArray());

    @Topic.AppLevel
    Topic<RobotEventListener> TOPIC = Topic.create("Robot Refresh", RobotEventListener.class);

    static @NotNull RobotService getInstance() {
        return ApplicationManager.getApplication().getService(RobotService.class);
    }

    /**
     * Checks if this plugin is currently connected to a robot. This asserts that a robot is currently both persisted and
     * connected to.
     *
     * @return if this plugin is currently connected to a robot.
     */
    boolean isConnected();

    /**
     * Returns the robot which is currently persisted, or connected.
     *
     * @return the robot which is currently persisted, or {@code null} if a robot is not persisted.
     */
    @Nullable RapidRobot getRobot();

    /**
     * Connects to the specified path with the specified credentials.
     *
     * @param path        the path to connect to.
     * @param credentials the credentials to authenticate with.
     * @return the connected robot.
     */
    @NotNull RapidRobot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException;

    /**
     * Disconnects the currently persisted robot, and deletes all persisted state associated with it.
     */
    void disconnect() throws IOException, InterruptedException;

    @Nullable RapidRobot.State getRobotState();

    void setRobotState(@Nullable RapidRobot.State robotState);

    class State {
        public RapidRobot.State state;
    }
}
