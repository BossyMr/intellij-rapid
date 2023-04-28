package com.bossymr.network.client;

import com.bossymr.network.NetworkAction;
import com.bossymr.network.NetworkManager;
import org.jetbrains.annotations.NotNull;

public interface TrackableNetworkManager extends NetworkManager {

    void track(@NotNull NetworkAction action);

}
