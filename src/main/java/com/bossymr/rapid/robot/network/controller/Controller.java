package com.bossymr.rapid.robot.network.controller;

import com.bossymr.rapid.robot.network.client.NetworkCall;
import com.bossymr.rapid.robot.network.client.NetworkClient;
import com.bossymr.rapid.robot.network.controller.event.EventLog;
import com.bossymr.rapid.robot.network.controller.io.InputOutput;
import com.bossymr.rapid.robot.network.controller.rapid.Rapid;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    private final InputOutput inputOutput = new InputOutput(this);

    public Controller(@NotNull NetworkClient networkClient) throws IOException {
        this.networkClient = networkClient;
        try {
            getIdentity().get();
        } catch (InterruptedException | ExecutionException e) {
            throw new IOException(e);
        }
    }

    public static @NotNull Controller connect(@NotNull URI path, @NotNull String username, @NotNull String password) throws IOException {
        return new Controller(NetworkClient.connect(path, username, password.toCharArray()));
    }

    public @NotNull URI getPath() {
        return getNetworkClient().getPath();
    }

    public @NotNull NetworkClient getNetworkClient() {
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

    public @NotNull InputOutput getInputOutput() {
        return inputOutput;
    }
}
