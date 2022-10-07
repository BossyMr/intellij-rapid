package com.bossymr.rapid.network.controller.io;

import com.bossymr.rapid.network.client.NetworkCall;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.Node;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class InputOutput extends Node {

    public InputOutput(@NotNull Controller controller) {
        super(controller);
    }

    public @NotNull CompletableFuture<List<Network>> getNetworks() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/iosystem/networks")).build();
        return getController().getNetworkClient().fetchAll(networkCall)
                .thenApplyAsync(entities -> entities.stream()
                        .map(entity -> {
                            URI path = entity.getLink("self").orElseThrow().path();
                            return new Network(getController(), path, entity.title());
                        })
                        .toList());
    }

}
