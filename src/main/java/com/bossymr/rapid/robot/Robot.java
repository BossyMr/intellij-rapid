package com.bossymr.rapid.robot;

import com.intellij.credentialStore.Credentials;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;

/**
 * A {@code Robot} represents a robot, and can be directly connected to the remote robot or provide a previously
 * persisted state. A {@code Robot} is represented by a {@link com.bossymr.rapid.robot.state.RobotState}, which is
 * persisted by {@link RobotService}.
 */
public interface Robot {

    /**
     * Returns the name of this robot.
     *
     * @return the name of this robot.
     */
    @NotNull String getName();

    /**
     * Returns the path to this robot.
     *
     * @return the path to this robot.
     */
    @NotNull URI getPath();

    /**
     * Checks if this robot is currently connected to the remote robot.
     *
     * @return if this robot is currently connected to the remote robot.
     */
    boolean isConnected();

    /**
     * Reconnects to this robot, using persisted credentials. This will reconstruct all persisted state for this robot.
     * If no persisted credentials are found for this robot, empty credentials are used instead.
     *
     * @throws IOException if an I/O error occurs.
     */
    void reconnect() throws IOException;

    /**
     * Reconnects to this robot, using the supplied credentials. This will reconstruct all persisted state for this
     * robot.
     *
     * @param credentials the credentials to authenticate with.
     * @throws IOException if an I/O error occurs.
     */
    void reconnect(@NotNull Credentials credentials) throws IOException;

    /**
     * Disconnects from this robot. This will close all active subscriptions for this robot, but will not impact the
     * persisted state for this robot.
     *
     * @throws IOException if an I/O error occurs.
     */
    void disconnect() throws IOException;

}
