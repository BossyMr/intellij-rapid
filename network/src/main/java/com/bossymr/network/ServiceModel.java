package com.bossymr.network;

import com.bossymr.network.client.NetworkEngine;
import org.jetbrains.annotations.NotNull;

public interface ServiceModel {

    static <T extends ServiceModel> @NotNull T move(@NotNull T service, @NotNull NetworkEngine networkEngine) {
        @SuppressWarnings("unchecked")
        Class<T> serviceType = (Class<T>) service.getClass().getInterfaces()[0];
        return networkEngine.createService(serviceType);
    }

    @NotNull NetworkEngine getNetworkEngine();

}
