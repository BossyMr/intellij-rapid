package com.bossymr.rapid.language.symbol;

import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.network.RobotService;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * A robot represents a remote robot. A robot might be directly connected to the remote robot, through a
 * {@link RobotService}. If a robot is not directly connected to the remote robot, symbols, tasks and modules are
 * persisted.
 */
public interface RapidRobot extends RapidSymbol {

    @NotNull URI getPath();

    /**
     * Returns the name of this robot.
     *
     * @return the name of this robot.
     */
    @NotNull String getName();

    /**
     * Returns all persisted symbols.
     *
     * @return all persisted symbols.
     */
    @NotNull Set<VirtualSymbol> getSymbols();

    /**
     * Returns all persisted tasks.
     *
     * @return all persisted tasks.
     */
    @NotNull List<RapidTask> getTasks();

    /**
     * Returns a symbol on this persisted robot, with the specified name. If the symbol was not found and if the robot
     * is currently connected, it will attempt to retrieve the symbol.
     *
     * @param name the name of the symbol.
     * @return a symbol on this robot with the specified name, or {@code null} if a symbol with the specified name was
     * not found.
     */
    @Nullable VirtualSymbol getSymbol(@NotNull String name);

    /**
     * Checks if this robot is currently connected to a remote robot.
     *
     * @return if this robot is currently connected.
     */
    boolean isConnected();

    /**
     * Returns a {@code RobotService} if this robot is connected.
     *
     * @return a {@code RobotService}, or {@code null} if this robot is not connected.
     */
    @Nullable RobotService getRobotService();

    /**
     * Reconnects to this robot, using the persisted path and credentials.
     *
     * @throws IOException if an I/O error occurs.
     */
    @NotNull RobotService reconnect() throws IOException, InterruptedException;

    /**
     * Reconnects to this robot, using the specified credentials.
     *
     * @param credentials the credentials to authenticate with.
     * @throws IOException if an I/O error occurs.
     */
    @NotNull RobotService reconnect(@NotNull Credentials credentials) throws IOException, InterruptedException;

    /**
     * Disconnects from this robot.
     *
     * @throws IOException if an I/O error occurs.
     */
    void disconnect() throws IOException, InterruptedException;

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
    void upload(@NotNull RapidTask task, @NotNull Collection<VirtualFile> modules) throws IOException, InterruptedException;

    /**
     * Downloads modules from the connected robot.
     *
     * @throws IOException if an I/O error occurs.
     * @throws InterruptedException if the operation is interrupted.
     * @throws IllegalStateException if the robot is not currently connected.
     */
    void download() throws IOException, InterruptedException;

}
