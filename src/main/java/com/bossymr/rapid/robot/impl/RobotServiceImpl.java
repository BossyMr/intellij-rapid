package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Optional;

@State(name = "robot", storages = {
        @Storage("robot/robot.xml")
})
public class RobotServiceImpl implements RobotService {

    private Robot robot;
    private RobotService.State state = new RobotService.State();

    @Override
    public @NotNull Optional<Robot> getRobot() {
        if (robot != null) return Optional.of(robot);
        if (state.robotState != null) {
            Controller controller = RobotUtil.getController(URI.create(state.robotState.path));
            return Optional.of(robot = new RobotImpl(state.robotState, controller));
        }
        return Optional.empty();
    }

    @Override
    public @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) {
        Controller controller = RobotUtil.getController(path, credentials);
        state.robotState = RobotUtil.getState(controller);
        robot = new RobotImpl(state.robotState, controller);
        return robot;
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
