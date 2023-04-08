package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"ios-network-li", "ios-network"})
public interface InputOutputNetwork {

    @Property("name")
    @NotNull String getName();

    @Property("pstate")
    @NotNull InputOutputPhysicalState getPhysicalState();

    @Property("lstate")
    @NotNull InputOutputLogicalState getLogicalState();

    @Fetch("{@devices}")
    @NotNull List<InputOutputDevice> getDevices();

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=set")
    @NotNull Void setState(
            @NotNull InputOutputLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkQuery<InputOutputNetworkEvent> onState();

    @Fetch("{@self}?resource=config")
    @NotNull InputOutputNetworkConfiguration getConfigurationType();

    @Fetch("{@self}?resource=config")
    @NotNull InputOutputNetworkConfiguration getConfigurationType(
            @NotNull @Field("configtype") InputOutputNetworkConfigurationRealm configurationType
    );

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=config")
    @NotNull Void setConfigurationType(
            @NotNull @Field("config-type") InputOutputNetworkConfigurationType configurationType
    );

}
