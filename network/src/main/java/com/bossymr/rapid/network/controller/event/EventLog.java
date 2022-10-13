package com.bossymr.rapid.network.controller.event;

import com.bossymr.rapid.network.client.NetworkCall;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.Node;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventLog extends Node {

    public EventLog(@NotNull Controller controller) {
        super(controller);
    }

    public @NotNull CompletableFuture<List<EventLogCategory>> getCategories() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/elog"))
                .putQuery("lang", "en")
                .putQuery("resource", "count")
                .build();
        return getController().getNetworkClient().fetchAll(networkCall)
                .thenApplyAsync(entities -> entities.stream()
                        .map(entity -> new EventLogCategory(getController(), entity.getLink("self").orElseThrow().path(), entity.title()))
                        .toList());
    }
}
