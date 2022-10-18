package com.bossymr.rapid.robot.network.controller.io;

import com.bossymr.rapid.robot.network.client.NetworkCall;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Device extends EntityNode<DeviceEntity> {

    private final URI path;
    private final String identifier;

    public Device(@NotNull Controller controller, @NotNull URI path, @NotNull String identifier) {
        super(controller);
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public @NotNull CompletableFuture<DeviceEntity> getEntity() {
        NetworkCall networkCall = NetworkCall.newBuilder(path).build();
        return getController().getNetworkClient().fetch(networkCall, DeviceEntity.class);
    }

    public @NotNull CompletableFuture<List<Signal>> getSignals() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/iosystem/signals"))
                .putQuery("device", identifier)
                .build();
        return getController().getNetworkClient().fetchAll(networkCall)
                .thenApplyAsync(entities -> entities.stream()
                        .map(entity -> {
                            URI path = entity.getLink("self").orElseThrow().path();
                            return new Signal(getController(), path, entity.title());
                        })
                        .toList());
    }
}
