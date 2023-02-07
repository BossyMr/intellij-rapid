package com.bossymr.rapid.robot.impl;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.client.DelegatingNetworkEngine;
import com.bossymr.network.client.NetworkEngine;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RobotDelegatingNetworkEngine extends DelegatingNetworkEngine {
    private static final Logger LOG = Logger.getInstance(RobotDelegatingNetworkEngine.class);

    public RobotDelegatingNetworkEngine(@NotNull NetworkEngine engine) {
        super(engine);
    }

    @Override
    protected <T> void onSuccess(@NotNull NetworkCall<T> request, @Nullable T response) {
        LOG.info("Request '" + request + "' succeeded");
    }

    @Override
    protected void onFailure(@NotNull NetworkCall<?> request, @NotNull Throwable throwable) {
        LOG.info("Request '" + request + "' failed '" + throwable + "'");
        RobotUtil.showNotification(null, request.request().uri());
    }

    @Override
    protected void onFailure(@NotNull Throwable throwable) {
        super.onFailure(throwable);
    }
}
