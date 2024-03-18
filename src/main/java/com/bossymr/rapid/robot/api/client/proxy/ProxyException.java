package com.bossymr.rapid.robot.api.client.proxy;

import org.jetbrains.annotations.NotNull;

public class ProxyException extends RuntimeException {

    public ProxyException(@NotNull String message) {
        super(message);
    }

    public ProxyException(@NotNull Throwable cause) {
        super(cause);
    }
}
