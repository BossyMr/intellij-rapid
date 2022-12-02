package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.RobotService;
import com.bossymr.rapid.robot.RobotState;
import com.bossymr.rapid.robot.network.Controller;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

@State(name = "robot",
        storages = {
                @Storage("robot.xml")
        })
public class RobotServiceImpl implements RobotService {

    private State state = new State();
    private Robot robot;

    @Override
    public @Nullable RobotState getRobotState() {
        return state.state;
    }

    @Override
    public void setRobotState(@Nullable RobotState robotState) {
        state.state = robotState;
    }

    @Override
    public @Nullable Robot getRobot() {
        if (robot != null) {
            return robot;
        }
        if (getRobotState() != null) {
            return robot = new RobotImpl(getRobotState());
        }
        return null;
    }

    @Override
    public @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException {
        RobotEventListener.publish().beforeConnect();
        Controller controller = Controller.connect(path, credentials);
        robot = new RobotImpl(controller);
        RobotEventListener.publish().afterConnect(robot);
        return robot;
    }

    @Override
    public void disconnect() {
        RobotEventListener.publish().beforeRemoval(robot);
        setRobotState(null);
        this.robot = null;
        RobotUtil.reload();
        RobotEventListener.publish().afterRemoval();
    }

    @Override
    public void dispose() {
        Robot robot = getRobot();
        if (robot != null) {
            try {
                robot.disconnect();
            } catch (IOException ex) {
                RobotUtil.showNotification(robot.getPath());
            }
        }
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
