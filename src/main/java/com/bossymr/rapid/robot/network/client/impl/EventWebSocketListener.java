package com.bossymr.rapid.robot.network.client.impl;

import com.bossymr.rapid.robot.network.client.model.CollectionModel;
import com.bossymr.rapid.robot.network.client.model.Model;
import com.bossymr.rapid.robot.network.client.model.ModelUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class EventWebSocketListener implements WebSocket.Listener {

    private final NetworkClientImpl networkClient;
    private StringBuilder stringBuilder = new StringBuilder();

    public EventWebSocketListener(@NotNull NetworkClientImpl networkClient) {
        this.networkClient = networkClient;
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        stringBuilder.append(data);
        if (last) {
            String event = stringBuilder.toString();
            try {
                CollectionModel collectionModel = ModelUtil.convert(event.getBytes());
                for (Model model : collectionModel.getModels()) {
                    networkClient.handleEntity(model);
                }
            } catch (IOException e) {
                return CompletableFuture.failedStage(e);
            }
            stringBuilder = new StringBuilder();
        }
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }
}
