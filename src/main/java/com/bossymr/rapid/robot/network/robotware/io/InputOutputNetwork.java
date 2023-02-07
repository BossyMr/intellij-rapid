package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"ios-network-li", "ios-network"})
public interface InputOutputNetwork extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("pstate")
    @NotNull InputOutputPhysicalState getPhysicalState();

    @Property("lstate")
    @NotNull InputOutputLogicalState getLogicalState();

    @GET("{@devices}")
    @NotNull NetworkCall<List<InputOutputDevice>> getDevices();

    @POST("{@self}?action=set")
    @NotNull NetworkCall<Void> setState(
            @NotNull InputOutputLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkCall<InputOutputNetworkEvent> onState();

    @GET("{@self}?resource=config")
    @NotNull NetworkCall<InputOutputNetworkConfiguration> getConfigurationType();

    @GET("{@self}?resource=config")
    @NotNull NetworkCall<InputOutputNetworkConfiguration> getConfigurationType(
            @NotNull @Field("configtype") InputOutputNetworkConfigurationRealm configurationType
    );

    @POST("{@self}?action=config")
    @NotNull NetworkCall<Void> setConfigurationType(
            @NotNull @Field("config-type") InputOutputNetworkConfigurationType configurationType
    );

}
