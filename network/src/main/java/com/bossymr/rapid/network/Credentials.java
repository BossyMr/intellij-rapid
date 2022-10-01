package com.bossymr.rapid.network;

import org.jetbrains.annotations.NotNull;

public record Credentials(@NotNull String username, char @NotNull [] password) {

    public Credentials(@NotNull String username, char @NotNull [] password) {
        this.username = username;
        this.password = password.clone();
    }
}