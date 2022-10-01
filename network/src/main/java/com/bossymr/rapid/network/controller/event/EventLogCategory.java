package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.NetworkQuery;
import com.bossymr.rapid.network.SubscribableEvent;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;

public class EventLogCategory extends EntityNode<EventLogCategoryEntity> {

    public final @NotNull URI path;

    public EventLogCategory(@NotNull Controller controller, @NotNull URI path) {
        super(controller);
        this.path = path;
    }

    @Override
    public @NotNull NetworkQuery<EventLogCategoryEntity> getEntity() {
        return null;
    }

    public @NotNull NetworkQuery<List<EventLogMessageEntity>> getMessages() {
        return null;
    }

    public @NotNull SubscribableEvent<EventLogMessageEntity> onMessage() {
        return null;
    }
}
