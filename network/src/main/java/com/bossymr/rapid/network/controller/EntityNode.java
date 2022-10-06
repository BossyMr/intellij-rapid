package com.bossymr.rapid.network.controller;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class EntityNode<T> extends Node {

    public EntityNode(@NotNull Controller controller) {
        super(controller);
    }

    public abstract @NotNull CompletableFuture<T> getEntity();

}
