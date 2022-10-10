package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.NetworkCall;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Task extends EntityNode<TaskEntity> {

    private final URI path;
    private final String identifier;


    public Task(@NotNull Controller controller, @NotNull URI path, @NotNull String identifier) {
        super(controller);
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public @NotNull CompletableFuture<TaskEntity> getEntity() {
        NetworkCall networkCall = NetworkCall.newBuilder(path).build();
        return getController().getNetworkClient().fetch(networkCall, TaskEntity.class);
    }

    public @NotNull CompletableFuture<List<Module>> getModules() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/rapid/modules"))
                .putQuery("task", identifier)
                .build();
        return getController().getNetworkClient().fetchAll(networkCall)
                .thenApplyAsync(entities -> entities.stream()
                        .map(entity -> new Module(getController(), entity.title()))
                        .toList());
    }
}
