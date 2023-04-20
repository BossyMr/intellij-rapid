package com.bossymr.network.client.proxy;

import com.bossymr.network.NetworkManager;
import org.jetbrains.annotations.Nullable;

public interface NetworkProxy {

    @Nullable NetworkManager getNetworkManager();

    void move(@Nullable NetworkManager manager);
}
