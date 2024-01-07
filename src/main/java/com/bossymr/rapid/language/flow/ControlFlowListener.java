package com.bossymr.rapid.language.flow;

import com.bossymr.rapid.language.flow.data.DataFlowState;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.EventListener;

public interface ControlFlowListener extends EventListener {

    static @NotNull ControlFlowListener publish() {
        return ApplicationManager.getApplication().getMessageBus().syncPublisher(ControlFlowService.TOPIC);
    }

    static void connect(@NotNull ControlFlowListener eventListener) {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(ControlFlowService.TOPIC, eventListener);
    }

    default void onState(@NotNull DataFlowState state) {}

    default void onBlock(@NotNull ControlFlowBlock block) {}

}
