package com.bossymr.rapid.robot.api.client.security;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

/**
 * {@code Credentials} represents a set of credentials containing a username and password, used for authentication.
 *
 * @param username the username.
 * @param password the password.
 */
public record Credentials(@NotNull String username, char @NotNull [] password) {

    public Credentials(@NotNull String username, @NotNull String password) {
        this(username, password.toCharArray());
    }

    public Credentials(@NotNull String username, char @NotNull [] password) {
        this.username = username;
        this.password = password.clone();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Credentials that = (Credentials) object;
        return Objects.equals(username, that.username) && Arrays.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(username);
        result = 31 * result + Arrays.hashCode(password);
        return result;
    }
}
