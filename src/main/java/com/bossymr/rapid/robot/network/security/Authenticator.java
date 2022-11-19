package com.bossymr.rapid.robot.network.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface Authenticator {

    @Nullable HttpRequest authenticate(@NotNull HttpRequest request);

    @Nullable HttpRequest authenticate(@NotNull HttpResponse<?> response);

}
