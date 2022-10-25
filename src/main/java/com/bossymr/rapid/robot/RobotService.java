package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.psi.RapidSymbol;
import com.bossymr.rapid.language.psi.RapidType;
import com.bossymr.rapid.robot.state.RobotState;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.Set;

/**
 * A {@code RobotService} is a project service which manages connections to remote robots, which are represented as a
 * {@link Robot} instance.
 */
public interface RobotService extends PersistentStateComponent<RobotService.State>, Disposable {

    @Topic.ProjectLevel
    Topic<RobotEventListener> TOPIC = Topic.create("Robot Refresh", RobotEventListener.class);

    /**
     * Returns the instance of this service which is associated with the specified project.
     *
     * @param project the project.
     * @return the service associated with the specified project.
     */
    static @NotNull RobotService getInstance(@NotNull Project project) {
        return project.getService(RobotService.class);
    }

    /**
     * Returns the symbols which are installed on the connected robot and symbols which are always guaranteed to be
     * available.
     *
     * @return the symbols on the connected robot.
     */
    @NotNull Set<RapidSymbol> getSymbols();

    /**
     * Returns the symbol on the connected robot, or a symbol which is always guaranteed to be available, with the
     * specified name.
     *
     * @param name the name of the symbol.
     * @return the symbol on the connected robot, with the specified name, if a symbol with the specified name is found.
     */
    @NotNull Optional<RapidSymbol> getSymbol(@NotNull String name);

    /**
     * Returns the currently persisted robot.
     *
     * @return the currently persisted robot.
     */
    @NotNull Optional<Robot> getRobot();

    /**
     * Returns the specified data type.
     *
     * @param dataType the data type.
     * @return the specified data type.
     */
    @NotNull RapidType getType(@NotNull DataType dataType);

    /**
     * Connects to a robot on the specified path, with the specified credentials. This will override all persisted state
     * of the previously connected robot. The supplied credentials are persisted for all connections to the specified
     * path.
     *
     * @param path the path to the remote robot.
     * @param credentials the credentials to authenticate to the robot.
     * @throws IOException if an I/O error occurs.
     */
    @NotNull Robot connect(@NotNull URI path, @NotNull Credentials credentials) throws IOException;

    /**
     * Deletes the state of the persisted robot, and disconnects from any connections.
     *
     * @throws IOException if an I/O error occurs.
     */
    void delete() throws IOException;

    /**
     * A {@code DataType} is a data type which is built into the language and guaranteed to always be available.
     */
    enum DataType {
        /**
         * A single-precision numeric data type.
         */
        NUMBER,

        /**
         * A double-precision numeric data type.
         */
        DOUBLE,

        /**
         * A string data type.
         */
        STRING,

        /**
         * A logical data type.
         */
        BOOLEAN,

        /**
         * A position data type.
         */
        POSITION,

        /**
         * An orientation data type.
         */
        ORIENTATION,

        /**
         * A transformation data type, consisting of both a position and an orientation element.
         */
        TRANSFORMATION;

        /**
         * Returns the type instance associated with this data type.
         *
         * @param project the project.
         * @return the type instance associated with this data type.
         */
        public @NotNull RapidType getType(@NotNull Project project) {
            return RobotService.getInstance(project).getType(this);
        }
    }

    /**
     * A {@code State} object represents the state of this service, which contains the state of a persisted robot
     * connection. The state is retrieved by called {@link #getState()} and stored by calling
     * {@link #loadState(Object) loadState(State)}.
     */
    class State {

        /**
         * The state of the currently persisted robot, or {@code null} if a robot is not currently persisted.
         */
        public RobotState robotState;
    }
}
