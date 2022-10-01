package com.bossymr.rapid.network.controller;

import com.bossymr.rapid.network.NetworkQuery;
import org.jetbrains.annotations.NotNull;

public abstract class EntityNode<T> extends Node {

    public EntityNode(@NotNull Controller controller) {
        super(controller);
    }

    public abstract @NotNull NetworkQuery<T> getEntity();

}
