package com.bossymr.rapid.robot;

import com.bossymr.rapid.robot.state.RobotState;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * A {@code RobotService} is a service which manages the connection to a remote robot.
 */
public interface RobotService extends PersistentStateComponent<RobotService.State> {

    static @NotNull RobotService getInstance(@NotNull Project project) {
        return project.getService(RobotService.class);
    }

    @NotNull Optional<Robot> getRobot();

    void disconnect() throws IOException;

    @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException;

    class State {
        public RobotState robotState;
    }
}
