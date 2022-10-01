package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.NetworkQuery;
import com.bossymr.rapid.network.controller.Node;
import com.bossymr.rapid.network.controller.Controller;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventLog extends Node {

    public EventLog(@NotNull Controller controller) {
        super(controller);
    }

    public @NotNull NetworkQuery<List<EventLogCategory>> getCategories() {
        return null;
    }
}
