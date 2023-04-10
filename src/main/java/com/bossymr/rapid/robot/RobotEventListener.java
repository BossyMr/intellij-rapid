package com.bossymr.rapid.robot;

import com.bossymr.network.client.NetworkManager;
import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface RobotEventListener extends EventListener {

    static @NotNull RobotEventListener publish() {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(RemoteRobotService.TOPIC);
    }

    static void connect(@NotNull RobotEventListener eventListener) {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(RemoteRobotService.TOPIC, eventListener);
    }

    /**
     * Called after a symbol is added to the robot.
     *
     * @param robot the robot.
     * @param symbol the symbol.
     */
    default void onSymbol(@NotNull RapidRobot robot, @NotNull VirtualSymbol symbol) {}

    /**
     * Called after a new robot is connected.
     *
     * @param robot the robot.
     */
    default void onConnect(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {}

    /**
     * Called after a robot is disconnected.
     *
     * @param robot the robot.
     */
    default void onDisconnect(@NotNull RapidRobot robot) {}

    /**
     * Called after a robot is removed.
     */
    default void onRemoval(@NotNull RapidRobot robot) {}

    /**
     * Called after a robot is refreshed.
     *
     * @param robot the robot.
     */
    default void onRefresh(@NotNull RapidRobot robot, @NotNull NetworkManager manager) {}

    default void onDownload(@NotNull RapidRobot robot) {}

    default void onUpload(@NotNull RapidRobot robot) {}
}
