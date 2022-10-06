package com.bossymr.rapid.network.controller.rapid;

import com.bossymr.rapid.network.client.NetworkCall;
import com.bossymr.rapid.network.client.NetworkMethod;
import com.bossymr.rapid.network.controller.Controller;
import com.bossymr.rapid.network.controller.Node;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Rapid extends Node {

    public Rapid(@NotNull Controller controller) {
        super(controller);
    }

    public @NotNull CompletableFuture<List<SymbolEntity>> getSymbols(@NotNull SymbolSearchQuery query) {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/rw/rapid/symbols"))
                .setMethod(NetworkMethod.POST)
                .putQuery("action", "search-symbols")
                .putField(query.getArguments())
                .build();
        return getController().getNetworkClient().fetchAll(networkCall, SymbolEntity.class);
    }
}
