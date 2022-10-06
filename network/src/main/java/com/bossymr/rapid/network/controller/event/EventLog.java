package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.Node;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventLog extends Node {

    public EventLog(@NotNull Controller controller) {
        super(controller);
    }

    public @NotNull CompletableFuture<List<EventLogCategory>> getCategories() {
        return null;
    }
}
