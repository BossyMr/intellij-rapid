package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.RapidRobot;
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

    default void onSymbol(@NotNull RapidRobot robot, @NotNull VirtualSymbol symbol) {}

    default void beforeConnect() {}

    default void afterConnect(@NotNull RapidRobot robot) {}

    default void beforeDisconnect(@NotNull RapidRobot robot) {}

    default void afterDisconnect(@NotNull RapidRobot robot) {}

    default void beforeRemoval(@NotNull RapidRobot robot) {}

    default void afterRemoval() {}

    default void beforeRefresh(@NotNull RapidRobot robot) {}

    default void afterRefresh(@NotNull RapidRobot robot) {}

}
