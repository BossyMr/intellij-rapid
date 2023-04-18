package com.bossymr.network.client.proxy;

import com.bossymr.network.client.NetworkAction;
import org.jetbrains.annotations.Nullable;

public interface NetworkProxy {

    @Nullable NetworkAction getNetworkManager();

    void move(@Nullable NetworkAction manager);
}
