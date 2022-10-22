package com.bossymr.rapid.robot;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;

/**
 * An endpoint which can be subscribed to provide events related to a connected robot.
 */
public interface RobotTopic {

    /**
     * The subscribable endpoint. To subscribe to the endpoint, use {@link #subscribe(Project, RobotTopic)}. To publish
     * an event to the endpoint, use {@link #publish(Project)}, and call the appropriate method on the provided
     * instance.
     */
    @Topic.ProjectLevel
    Topic<RobotTopic> TOPIC = Topic.create("Robot", RobotTopic.class);

    /**
     * Subscribes to this endpoint, with the specified event consumer.
     *
     * @param project the project.
     * @param onEvent the event consumer.
     */
    static void subscribe(@NotNull Project project, @NotNull RobotTopic onEvent) {
        project.getMessageBus().connect().subscribe(TOPIC, onEvent);
    }

    /**
     * Creates an instance of this topic. To broadcast the event, call the appropriate method on the method on the
     * provided instance.
     *
     * @param project the project.
     * @return an instance of this event consumer.
     */
    static @NotNull RobotTopic publish(@NotNull Project project) {
        return project.getMessageBus().syncPublisher(TOPIC);
    }

    /**
     * This event is called on a robot connection.
     *
     * @param robot the connected robot.
     */
    default void onConnect(@NotNull Robot robot) {}

    /**
     * This event is called on a robot disconnection.
     *
     * @param robot the disconnected robot.
     */
    default void onDisconnect(@NotNull Robot robot) {}

    /**
     * This event is called on a robot removal.
     */
    default void onDisconnect() {}

    /**
     * This event is called on a robot reconnection.
     *
     * @param robot the reconnected robot.
     */
    default void onRefresh(@NotNull Robot robot) {}
}
