package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.RapidTask;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.network.RobotService;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

/**
 * A robot represents a remote robot. A robot might be directly connected to the remote robot, through a
 * {@link RobotService}. If a robot is not directly connected to the remote robot, symbols, tasks and modules are
 * persisted.
 */
public interface Robot {

    /**
     * Returns the name of this robot.
     *
     * @return the name of this robot.
     */
    @NotNull String getName();

    /**
     * Returns the path of this robot.
     *
     * @return the path of this robot.
     */
    @NotNull URI getPath();

    /**
     * Returns all symbols on this persisted robot.
     *
     * @return all symbols on this robot.
     */
    @NotNull Set<VirtualSymbol> getSymbols();

    /**
     * Returns all tasks on this persisted robot.
     *
     * @return all tasks on this robot.
     */
    @NotNull List<RapidTask> getTasks();

    /**
     * Returns a symbol on this persisted robot, with the specified name. If the symbol was not found and if the robot
     * is currently connected, it will attempt to retrieve the symbol.
     *
     * @param name the name of the symbol.
     * @return a symbol on this robot with the specified name, or {@code null} if a symbol with the specified name was
     * not found.
     * @throws IOException if an I/O error occurs.
     */
    @Nullable VirtualSymbol getSymbol(@NotNull String name) throws IOException, InterruptedException;

    /**
     * Returns the controller to the connected robot, if the robot is currently connected.
     *
     * @return the controller to the connected robot, or {@code null} if the robot is not currently connected.
     */
    @Nullable RobotService getRobotService();

    /**
     * Reconnects to this robot, using the persisted path and credentials.
     *
     * @throws IOException if an I/O error occurs.
     */
    void reconnect() throws IOException, InterruptedException;

    /**
     * Reconnects to this robot, using the specified credentials.
     *
     * @param credentials the credentials to authenticate with.
     * @throws IOException if an I/O error occurs.
     */
    void reconnect(@NotNull Credentials credentials) throws IOException, InterruptedException;

    /**
     * Uploads persisted modules to the remote robot.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     * @throws IllegalStateException if the robot is not currently connected.
     */
    void upload() throws IOException, InterruptedException;

    /**
     * Uploads the specified modules to the specified task on the remote robot.
     *
     * @param task the task.
     * @param modules the modules to upload.
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     * @throws IllegalStateException if the robot is not currently connected.
     */
    void upload(@NotNull RapidTask task, @NotNull Set<VirtualFile> modules) throws IOException, InterruptedException;

    /**
     * Downloads modules from the connected robot.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     * @throws IllegalStateException if the robot is not currently connected.
     */
    void download() throws IOException, InterruptedException;

    /**
     * Disconnects from this robot.
     *
     * @throws IOException if an I/O error occurs.
     */
    void disconnect() throws IOException;

}
