package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.RobotTopic;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

@State(name = "robot", storages = {
        @Storage("robot/robot.xml")
})
public class RobotServiceImpl implements RobotService {

    private static final Logger LOG = Logger.getInstance(RobotService.class);
    private final Project project;
    private Robot robot;
    private RobotService.State state = new RobotService.State();

    public RobotServiceImpl(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @NotNull Optional<Robot> getRobot() {
        if (robot != null) {
            LOG.debug("Retrieving connected robot: " + state.robotState.path);
            return Optional.of(robot);
        }
        if (state.robotState != null) {
            LOG.debug("Connecting to persisted robot: " + state.robotState.path);
            return Optional.of(robot = new RobotImpl(project, state.robotState));
        }
        return Optional.empty();
    }

    @Override
    public void disconnect() throws IOException {
        if (state.robotState != null) {
            LOG.info("Disconnecting from persisted robot: " + state.robotState.path);
            Robot robot = getRobot().orElseThrow();
            if (robot.isConnected()) robot.disconnect();
            this.state.robotState = null;
            this.robot = null;
            getTopic().onDisconnect();
        } else {
            LOG.warn("Attempt to disconnect to non-existent robot");
        }
    }

    @Override
    public @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException {
        LOG.debug("Connecting to new robot:" + path);
        Controller controller = RobotUtil.getController(path, credentials);
        state.robotState = RobotUtil.getState(controller);
        robot = new RobotImpl(project, state.robotState, controller);
        getTopic().onConnect(robot);
        return robot;
    }

    private @NotNull RobotTopic getTopic() {
        return project.getMessageBus().syncPublisher(RobotTopic.ROBOT_TOPIC);
    }

    @Override
    public @Nullable RobotService.State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
