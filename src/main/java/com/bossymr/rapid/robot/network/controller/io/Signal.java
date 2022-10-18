package com.bossymr.rapid.robot.network.controller.io;

import com.bossymr.rapid.robot.network.client.NetworkCall;
import com.bossymr.rapid.robot.network.controller.Controller;
import com.bossymr.rapid.robot.network.controller.EntityNode;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Signal extends EntityNode<SignalEntity> {

    private final URI path;
    private final String identifier;

    public Signal(@NotNull Controller controller, @NotNull URI path, @NotNull String identifier) {
        super(controller);
        this.path = path;
        this.identifier = identifier;
    }

    @Override
    public @NotNull CompletableFuture<SignalEntity> getEntity() {
        NetworkCall networkCall = NetworkCall.newBuilder(path).build();
        return getController().getNetworkClient().fetch(networkCall, SignalEntity.class);
    }
}
