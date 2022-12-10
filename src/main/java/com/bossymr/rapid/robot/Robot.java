package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.bossymr.rapid.robot.network.RobotService;
import com.intellij.credentialStore.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * A {@code Robot} is a persisted robot, which might be connected.
 */
public interface Robot {

    /**
     * Returns the name of this robot.
     *
     * @return the name of this robot.
     */
    @NotNull String getName();

    @NotNull URI getPath();

    /**
     * Returns all symbols on this persisted robot.
     *
     * @return all symbols on this robot.
     */
    @NotNull Set<VirtualSymbol> getSymbols();

    /**
     * Returns a symbol on this persisted robot, with the specified name. If the symbol was not found and if the robot
     * is currently connected, it will attempt to retrieve the symbol.
     *
     * @param name the name of the symbol.
     * @return a symbol on this robot with the specified name, or {@code null} if a symbol with the specified name was
     * not found.
     * @throws IOException if an I/O error occurs.
     */
    @Nullable VirtualSymbol getSymbol(@NotNull String name) throws IOException;

    /**
     * Returns the controller to the connected robot, if the robot is currently connected.
     *
     * @return the controller to the connected robot, or {@code null} if the robot is not currently connected.
     */
    @Nullable RobotService getController();

    /**
     * Reconnects to this robot, using the persisted path and credentials.
     *
     * @throws IOException if an I/O error occurs.
     */
    void reconnect() throws IOException;

    /**
     * Reconnects to this robot, using the specified credentials.
     *
     * @param credentials the credentials to authenticate with.
     * @throws IOException if an I/O error occurs.
     */
    void reconnect(@NotNull Credentials credentials) throws IOException;

    void disconnect() throws IOException;

    boolean isConnected();

}
