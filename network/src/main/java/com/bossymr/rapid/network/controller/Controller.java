package com.bossymr.rapid.network.controller;

import com.bossymr.rapid.network.Credentials;
import com.bossymr.rapid.network.client.NetworkCall;
import com.bossymr.rapid.network.client.NetworkClient;
import com.bossymr.rapid.network.controller.event.EventLog;
import com.bossymr.rapid.network.controller.rapid.Rapid;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.concurrent.CompletableFuture;

/**
 * A {@code Controller} represents a remote robot.
 * <p>
 * Network requests are provided to the client, who can choose to send the request asynchronously or synchronously.
 * Subscribable events are also provided to the client.
 */
public class Controller {

    private final NetworkClient networkClient;

    private final Rapid rapid = new Rapid(this);
    private final EventLog eventLog = new EventLog(this);

    public Controller(@NotNull NetworkClient networkClient) {
        this.networkClient = networkClient;
    }

    public static @NotNull Controller connect(@NotNull URI path, @NotNull Credentials credentials) {
        return new Controller(NetworkClient.connect(path, credentials.username(), credentials.password()));
    }

    public NetworkClient getNetworkClient() {
        return networkClient;
    }

    public @NotNull CompletableFuture<IdentityEntity> getIdentity() {
        NetworkCall networkCall = NetworkCall.newBuilder(URI.create("/ctrl/identity")).build();
        return networkClient.fetch(networkCall, IdentityEntity.class);
    }

    public @NotNull Rapid getRapid() {
        return rapid;
    }

    public @NotNull EventLog getEventLog() {
        return eventLog;
    }

}
