package com.bossymr.network.client.proxy;

import org.jetbrains.annotations.NotNull;

public class ProxyException extends RuntimeException {

    public ProxyException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }

    public ProxyException(String message) {
        super(message);
    }

    public ProxyException(@NotNull Throwable cause) {
        super(cause);
    }
}
