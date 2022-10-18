package com.bossymr.rapid.robot.network.controller;

import org.jetbrains.annotations.NotNull;

public abstract class Node {

    private final Controller controller;

    public Node(@NotNull Controller controller) {
        this.controller = controller;
    }

    public @NotNull Controller getController() {
        return controller;
    }
}
