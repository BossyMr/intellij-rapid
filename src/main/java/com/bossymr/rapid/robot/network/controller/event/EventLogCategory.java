package com.bossymr.rapid.robot.network.controller.event;

import com.bossymr.rapid.robot.network.SubscribableEvent;
import com.bossymr.rapid.robot.network.client.NetworkCall;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventLogCategory extends EntityNode<EventLogCategoryEntity> {

    private final URI path;
    private final String identifier;

    public EventLogCategory(@NotNull Controller controller, URI path, String identifier) {
        super(controller);
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public @NotNull CompletableFuture<EventLogCategoryEntity> getEntity() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/elog"))
                .putQuery("lang", "en")
                .putQuery("resource", "count")
                .build();
        return getController().getNetworkClient().fetchAll(networkCall, EventLogCategoryEntity.class)
                .thenApplyAsync(entities -> {
                    for (EventLogCategoryEntity entity : entities) {
                        if (entity.title().equals(identifier)) {
                            return entity;
                        }
                    }
                    throw new IllegalStateException();
                });
    }

    public @NotNull CompletableFuture<List<EventLogMessageEntity>> getMessages() {
        NetworkCall networkCall = NetworkCall.newBuilder(path)
                .build();
        return getController().getNetworkClient().fetchAll(networkCall, EventLogMessageEntity.class);
    }

    public @NotNull SubscribableEvent<EventLogMessageEntity> onMessage() {
        return null;
    }
}
