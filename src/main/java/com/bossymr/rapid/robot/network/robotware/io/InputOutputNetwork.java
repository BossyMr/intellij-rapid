package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.*;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
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
    @NotNull Query<List<InputOutputDevice>> getDevices();

    @POST("{@self}?action=set")
    @NotNull Query<Void> setState(
            @NotNull InputOutputLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableQuery<InputOutputNetworkEvent> onState();

    @GET("{@self}?resource=config")
    @NotNull Query<InputOutputNetworkConfiguration> getConfigurationType();

    @GET("{@self}?resource=config")
    @NotNull Query<InputOutputNetworkConfiguration> getConfigurationType(
            @NotNull @Field("configtype") InputOutputNetworkConfigurationRealm configurationType
    );

    @POST("{@self}?action=config")
    @NotNull Query<Void> setConfigurationType(
            @NotNull @Field("config-type") InputOutputNetworkConfigurationType configurationType
    );

}
