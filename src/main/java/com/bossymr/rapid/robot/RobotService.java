package com.bossymr.rapid.robot;

import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;

public interface RobotService extends PersistentStateComponent<RobotService.State>, Disposable {

    @Topic.AppLevel
    Topic<RobotEventListener> TOPIC = Topic.create("Robot Refresh", RobotEventListener.class);

    static @NotNull RobotService getInstance() {
        return ApplicationManager.getApplication().getService(RobotService.class);
    }

    @Nullable RobotState getRobotState();

    void setRobotState(@Nullable RobotState robotState);

    @Nullable Robot getRobot();

    @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException;

    void disconnect() throws IOException;

    class State {
        public RobotState state;
    }
}
