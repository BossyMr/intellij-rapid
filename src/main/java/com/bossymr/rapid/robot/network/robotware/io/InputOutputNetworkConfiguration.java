package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ios-network-config-runtime")
public interface InputOutputNetworkConfiguration extends EntityModel {

    @Property("networkname")
    @NotNull String getNetworkName();

    @Property("networktype")
    @NotNull String getNetworkType();

    @Property("networkaddress")
    @NotNull String getNetworkAddress();

}
