package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.client.NetworkClient;
import org.jetbrains.annotations.NotNull;

public interface ServiceModel {

    @NotNull NetworkClient getNetworkClient();

}
