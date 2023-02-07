package com.bossymr.rapid.robot.impl;

import com.bossymr.network.client.security.Credentials;
import com.bossymr.rapid.robot.RemoteRobotService;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.RobotState;
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
public class RemoteRobotServiceImpl implements RemoteRobotService {

    private @NotNull State state = new State();

    private @Nullable RobotImpl robot;

    @Override
    public @Nullable Robot getRobot() {
        if (robot != null) {
            return robot;
        }
        RobotState robotState = getRobotState();
        if (robotState != null) {
            return robot = new RobotImpl(robotState);
        }
        return null;
    }

    @Override
    public @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        RobotEventListener.publish().beforeConnect();
        RobotUtil.setCredentials(path, credentials.username(), credentials.password());
        return robot = new RobotImpl(path, credentials.username(), credentials.password());
    }

    @Override
    public void disconnect() throws IOException, InterruptedException {
        Robot robot = getRobot();
        if (robot != null) {
            RobotEventListener.publish().beforeRemoval(robot);
            robot.disconnect();
            RobotUtil.remove();
            this.robot = null;
            setRobotState(null);
            RobotEventListener.publish().afterRemoval();
        }
    }

    @Override
    public void dispose() {
        if (robot != null) {
            try {
                robot.dispose();
            } catch (IOException | InterruptedException ignored) {
            }
        }
    }

    @Override
    public @Nullable RobotState getRobotState() {
        return getState().state;
    }

    @Override
    public void setRobotState(@Nullable RobotState robotState) {
        getState().state = robotState;
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }
}
