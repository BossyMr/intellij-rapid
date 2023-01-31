package com.bossymr.network;

import com.bossymr.network.client.NetworkEngine;
import org.jetbrains.annotations.NotNull;

public interface ServiceModel {

    @NotNull NetworkEngine getNetworkEngine();

}
