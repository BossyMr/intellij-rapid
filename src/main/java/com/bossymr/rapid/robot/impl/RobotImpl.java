package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.psi.RapidSymbol;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.state.RobotState;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RobotImpl implements Robot {

    private final URI path;

    private Map<String, RapidSymbol> symbols;
    private Controller controller;

    public RobotImpl(@NotNull RobotState robotState, @NotNull Controller controller) {
        this.path = URI.create(robotState.path);
        this.controller = controller;
        this.symbols = RobotUtil.getSymbols(robotState);
    }

    private void build() {
        controller = RobotUtil.getController(path);
        RobotState robotState = RobotUtil.getState(controller);
        symbols = RobotUtil.getSymbols(robotState);
    }

    @Override
    public @NotNull Set<RapidSymbol> getSymbols() {
        return Set.copyOf(symbols.values());
    }

    @Override
    public @NotNull Optional<RapidSymbol> getSymbol(@NotNull String name) {
        return symbols.containsKey(name) ? Optional.of(symbols.get(name)) : Optional.empty();
    }

    @Override
    public boolean isConnected() {
        return controller != null;
    }

    @Override
    public void reconnect() {
        controller = RobotUtil.getController(path);
        build();
    }

    @Override
    public void disconnect() {
        controller = null;
    }
}
