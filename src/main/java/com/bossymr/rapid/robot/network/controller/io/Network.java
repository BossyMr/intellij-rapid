package com.bossymr.rapid.robot.network.controller.io;

import com.bossymr.rapid.robot.network.client.NetworkCall;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Network extends EntityNode<NetworkEntity> {

    private final URI path;
    private final String identifier;

    public Network(@NotNull Controller controller, @NotNull URI path, @NotNull String identifier) {
        super(controller);
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public @NotNull CompletableFuture<NetworkEntity> getEntity() {
        NetworkCall networkCall = NetworkCall.newBuilder(path).build();
        return getController().getNetworkClient().fetch(networkCall, NetworkEntity.class);
    }

    public @NotNull CompletableFuture<List<Device>> getDevices() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/iosystem/devices"))
                .putQuery("network", identifier)
                .build();
        return getController().getNetworkClient().fetchAll(networkCall)
                .thenApplyAsync(entities -> entities.stream()
                        .map(entity -> {
                            URI path = entity.getLink("self").orElseThrow().path();
                            return new Device(getController(), path, entity.title());
                        })
                        .toList());
    }
}
