package com.bossymr.network.client.proxy;

import com.bossymr.network.NetworkManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NetworkProxy {

    @Nullable NetworkManager getNetworkManager();

    @NotNull NetworkProxy move(@Nullable NetworkManager manager);
}
