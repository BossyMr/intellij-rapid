package com.bossymr.network.client.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * An {@code Authenticator} is used to authenticate a request.
 */
public interface Authenticator {

    /**
     * Attempts to authenticate the specified request.
     *
     * @param request the request to authenticate
     * @return the authenticated request, or {@code null} if the request could not be authenticated.
     */
    @Nullable HttpRequest authenticate(@NotNull HttpRequest request);

    /**
     * Attempts to retrieve the authentication challenge in the specified response, and reauthenticate its original
     * request. This method is called on unsuccessful responses to requests which were authenticated by this
     * authenticator, and should attempt to reauthenticate the request if possible (such as if a challenge is stale). If
     * the request cannot be re-authenticated, {@code null} should be returned, in which case the unsuccesful repsonse
     * will be returned.
     *
     * @param response the unsuccessful response.
     * @return the authenticated request, or {@code null} if the request could not be authenticated.
     */
    @Nullable HttpRequest authenticate(@NotNull HttpResponse<?> response);

}
