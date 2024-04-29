package com.bossymr.network.client.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * An {@code Authenticator} authenticates network requests.
 */
public interface Authenticator {

    /**
     * Preemptively authenticates the specified request. This is called for each request.
     *
     * @param request the request to authenticate.
     * @return the authenticated request, or {@code null} if the request could not be preemptively authenticated.
     */
    @Nullable HttpRequest authenticate(@NotNull HttpRequest request);

    /**
     * Authenticate the specified response. This is called the first time a response is received with a {@code 401} or
     * {@code 407} status code.
     *
     * @param response the response to authenticate.
     * @return the authenticated request, or {@code null} if the request could not be authenticated.
     */
    @Nullable HttpRequest authenticate(@NotNull HttpResponse<?> response);

}
