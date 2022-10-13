package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.NetworkCall;
import com.bossymr.rapid.network.client.NetworkMethod;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class Module extends EntityNode<ModuleEntity> {

    private final String identifier;

    public Module(@NotNull Controller controller, @NotNull String identifier) {
        super(controller);
        this.identifier = identifier;
    }

    @Override
    public @NotNull CompletableFuture<ModuleEntity> getEntity() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/rapid/modules"))
                .putQuery("task", identifier.split("/")[0])
                .build();
        return getController().getNetworkClient().fetchAll(networkCall, ModuleEntity.class)
                .thenApplyAsync(entities -> {
                    for (ModuleEntity entity : entities) {
                        if (entity.title().equals(identifier)) {
                            return entity;
                        }
                    }
                    throw new IllegalStateException();
                });
    }

    public @NotNull CompletableFuture<Void> save(@NotNull Path path) {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/rapid/modules/" + identifier.split("/")[1]))
                .setMethod(NetworkMethod.POST)
                .putQuery("task", identifier.split("/")[0])
                .putQuery("action", "save")
                .putField("name", identifier.split("/")[1])
                .putField("path", path.toString()) // path.resolve(identifier.split("/")[1] + ".mod").toString())
                .build();
        return getController().getNetworkClient().send(networkCall);
    }
}
