package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.language.psi.RapidSymbol;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotTopic;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.state.RobotState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RobotImpl implements Robot {

    private static final Logger LOG = Logger.getInstance(Robot.class);

    private final Project project;

    private final URI path;

    private String name;
    private Map<String, RapidSymbol> symbols;
    private Controller controller;

    public RobotImpl(@NotNull Project project, @NotNull RobotState robotState) {
        this(project, robotState, null);
    }

    public RobotImpl(@NotNull Project project, @NotNull RobotState robotState, @Nullable Controller controller) {
        this.project = project;
        this.path = URI.create(robotState.path);
        this.controller = controller;
        this.symbols = RobotUtil.getSymbols(robotState);
        this.name = robotState.name;
        getTopic().onRefresh(this);
    }

    private void build() throws IOException {
        controller = RobotUtil.getController(path);
        RobotState robotState = RobotUtil.getState(controller);
        symbols = RobotUtil.getSymbols(robotState);
        name = robotState.name;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @NotNull URI getPath() {
        return path;
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
    public void reconnect() throws IOException {
        LOG.debug("Reconnecting to robot: " + path);
        controller = RobotUtil.getController(path);
        build();
        getTopic().onRefresh(this);
    }

    private @NotNull RobotTopic getTopic() {
        return project.getMessageBus().syncPublisher(RobotTopic.ROBOT_TOPIC);
    }

    @Override
    public void disconnect() {
        LOG.debug("Disconnect to robot: " + path);
        controller = null;
    }
}
