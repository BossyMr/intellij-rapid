package com.bossymr.rapid.robot.api.client.proxy;

import com.bossymr.rapid.robot.api.NetworkManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NetworkProxy {

    @Nullable NetworkManager getNetworkManager();

    @NotNull NetworkProxy move(@Nullable NetworkManager manager);
}
