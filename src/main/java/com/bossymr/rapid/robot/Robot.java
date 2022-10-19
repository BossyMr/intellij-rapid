package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.psi.RapidSymbol;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;

/**
 * A {@code Robot} represents a connected robot.
 */
public interface Robot {

    @NotNull String getName();

    @NotNull URI getPath();

    @NotNull Set<RapidSymbol> getSymbols();

    @NotNull Optional<RapidSymbol> getSymbol(@NotNull String name);

    boolean isConnected();

    void reconnect() throws IOException;

    void disconnect() throws IOException;

}
