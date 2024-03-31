package com.bossymr.network.client.security;

import org.jetbrains.annotations.NotNull;

public record Credentials(@NotNull String username, char @NotNull [] password) {

    public Credentials(@NotNull String username, char @NotNull [] password) {
        this.username = username;
        this.password = password.clone();
    }

    public Credentials(@NotNull String username, @NotNull String password) {
        this(username, password.toCharArray());
    }
}
