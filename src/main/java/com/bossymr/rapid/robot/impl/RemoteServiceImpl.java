package com.bossymr.rapid.robot.impl;

import com.bossymr.rapid.robot.PersistentRobotState;
import com.bossymr.rapid.robot.RemoteService;
import com.bossymr.rapid.robot.Robot;
import com.bossymr.rapid.robot.RobotEventListener;
import com.bossymr.rapid.robot.network.RobotService;
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
public class RemoteServiceImpl implements RemoteService {

    private @NotNull State state = new State();

    private @Nullable RobotImpl robot;

    @Override
    public @Nullable Robot getRobot() {
        if (robot != null) {
            return robot;
        }
        PersistentRobotState robotState = getRobotState();
        if (robotState != null) {
            return robot = new RobotImpl(robotState);
        }
        return null;
    }

    @Override
    public @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException, InterruptedException {
        RobotEventListener.publish().beforeConnect();
        RobotUtil.setCredentials(path, credentials);
        RobotService service = RobotService.connect(path, credentials);
        return robot = new RobotImpl(service);
    }

    @Override
    public void disconnect() throws IOException {
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
            } catch (IOException ignored) {}
        }
    }

    @Override
    public @Nullable PersistentRobotState getRobotState() {
        return getState().state;
    }

    @Override
    public void setRobotState(@Nullable PersistentRobotState robotState) {
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
