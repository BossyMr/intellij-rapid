package com.bossymr.network.client.proxy;

import org.jetbrains.annotations.NotNull;

public class ProxyException extends RuntimeException {

    public ProxyException(@NotNull String message) {
        super(message);
    }

    public ProxyException(@NotNull Throwable cause) {
        super(cause);
    }
}
