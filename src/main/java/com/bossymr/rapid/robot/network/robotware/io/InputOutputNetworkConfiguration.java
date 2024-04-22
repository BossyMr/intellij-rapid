package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ios-network-config-runtime")
public interface InputOutputNetworkConfiguration {

    @Property("networkname")
    @NotNull String getNetworkName();

    @Property("networktype")
    @NotNull String getNetworkType();

    @Property("networkaddress")
    @NotNull String getNetworkAddress();

}
