package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.RobotState.SymbolState;
import com.bossymr.rapid.robot.impl.RobotUtil;
import com.bossymr.rapid.robot.network.impl.ControllerImpl;
import com.intellij.credentialStore.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * A {@code Controller} is a connected robot.
 */
public interface Controller extends Closeable {

    Credentials DEFAULT_CREDENTIALS = new Credentials("Default User", "robotics");

    static @NotNull Controller connect(@NotNull URI path, @NotNull Credentials credentials) {
        RobotUtil.setCredentials(path, credentials);
        return new ControllerImpl(path, credentials);
    }

    /**
     * Returns the path of this robot.
     *
     * @return the path of this robot.
     */
    @NotNull URI getPath();

    /**
     * Returns the name of this robot.
     *
     * @return the name of this robot.
     * @throws IOException if an I/O error occurs.
     */
    @NotNull String getName() throws IOException;

    /**
     * Returns all symbols which are installed on this robot.
     *
     * @return a collection containing all symbols on this robot.
     * @throws IOException if an I/O error occurs.
     */
    @NotNull Set<SymbolState> getSymbols() throws IOException;

    /**
     * Returns a symbol installed on this robot with the specified name.
     *
     * @param name the name of the symbol.
     * @return a symbol on this robot with the specified name, or {@code null} if a symbol with the specified name was
     * not found.
     * @throws IOException if an I/O error occurs;
     */
    @Nullable SymbolState getSymbol(@NotNull String name) throws IOException;
}
