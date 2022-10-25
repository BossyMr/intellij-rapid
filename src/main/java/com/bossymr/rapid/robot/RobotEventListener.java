package com.bossymr.rapid.robot;

import org.jetbrains.annotations.NotNull;

public interface RobotEventListener {

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
     *
     * @param robot the removed robot.
     */
    default void onRemoval(@NotNull Robot robot) {}

    /**
     * This event is called on a robot reconnection.
     *
     * @param robot the reconnected robot.
     */
    default void onRefresh(@NotNull Robot robot) {}
}
