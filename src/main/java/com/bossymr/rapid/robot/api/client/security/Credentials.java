package com.bossymr.rapid.robot.api.client.security;

import org.jetbrains.annotations.NotNull;

/**
 * {@code Credentials} represents a set of credentials containing a username and password, used for authentication.
 *
 * @param username the username.
 * @param password the password.
 */
public record Credentials(@NotNull String username, char @NotNull [] password) {

    public Credentials(@NotNull String username, char @NotNull [] password) {
        this.username = username;
        this.password = password.clone();
    }
}
