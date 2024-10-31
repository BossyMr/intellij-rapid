package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.annotations.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * A {@code ProxyException} indicates that an exception occurred in an {@link Entity entity} proxy.
 */
public class ProxyException extends RuntimeException {

    public ProxyException(@NotNull String message) {
        super(message);
    }

    public ProxyException(@NotNull Throwable cause) {
        super(cause);
    }

    public ProxyException(String message, Throwable cause) {
        super(message, cause);
    }
}
