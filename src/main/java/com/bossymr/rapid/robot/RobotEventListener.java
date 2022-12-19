package com.bossymr.rapid.robot;

import com.bossymr.rapid.language.symbol.virtual.VirtualSymbol;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface RobotEventListener extends EventListener {

    static @NotNull RobotEventListener publish() {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(RemoteService.TOPIC);
    }

    static void connect(@NotNull RobotEventListener eventListener) {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(RemoteService.TOPIC, eventListener);
    }

    default void onSymbol(@NotNull Robot robot, @NotNull VirtualSymbol symbol) {}

    default void beforeConnect() {}

    default void afterConnect(@NotNull Robot robot) {}

    default void beforeDisconnect(@NotNull Robot robot) {}

    default void afterDisconnect(@NotNull Robot robot) {}

    default void beforeRemoval(@NotNull Robot robot) {}

    default void afterRemoval() {}

    default void beforeRefresh(@NotNull Robot robot) {}

    default void afterRefresh(@NotNull Robot robot) {}

}
