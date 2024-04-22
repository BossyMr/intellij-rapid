package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.*;
import com.bossymr.rapid.robot.api.client.FetchMethod;
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
  @NotNull NetworkQuery<List<InputOutputDevice>> getDevices();

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=set")
  @NotNull NetworkQuery<Void> setState(@NotNull InputOutputLogicalState logicalState);

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkQuery<InputOutputNetworkEvent> onState();

        @Fetch("{@self}?resource=config")
  @NotNull NetworkQuery<InputOutputNetworkConfiguration> getConfigurationType();

        @Fetch("{@self}?resource=config")
  @NotNull NetworkQuery<InputOutputNetworkConfiguration> getConfigurationType(@NotNull @Field("configtype") InputOutputNetworkConfigurationRealm configurationType);

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=config")
  @NotNull NetworkQuery<Void> setConfigurationType(@NotNull @Field("config-type") InputOutputNetworkConfigurationType configurationType);

}
