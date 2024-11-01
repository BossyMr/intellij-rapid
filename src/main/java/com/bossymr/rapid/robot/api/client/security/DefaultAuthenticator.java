package com.bossymr.rapid.robot.api.client.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * An authenticator that will not authenticate any requests. This authenticator is used in situations where an
 * authenticator is expected, but not required.
 */
public class DefaultAuthenticator implements Authenticator {
    @Override
    public @Nullable HttpRequest authenticate(@NotNull HttpRequest request) {
        return null;
    }

    @Override
    public @Nullable HttpRequest authenticate(@NotNull HttpResponse<?> response) {
        return null;
    }
}
