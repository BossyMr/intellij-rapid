package com.bossymr.network;

import com.bossymr.network.client.NetworkFactory;
import org.jetbrains.annotations.NotNull;

public interface ServiceModel {

    @NotNull NetworkFactory getNetworkFactory();

}
