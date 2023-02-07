package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"ios-device", "ios-device-li"})
public interface InputOutputDevice extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("pstate")
    @NotNull InputOutputPhysicalState getPhysicalState();

    @Property("lstate")
    @NotNull InputOutputLogicalState getLogicalState();

    @Property("address")
    @NotNull String getAddress();

    @Property("indata")
    @NotNull String getInputData();

    @Property("inmask")
    @NotNull String getInputMask();

    @Property("outdata")
    @NotNull String getOutputData();

    @Property("outmask")
    @NotNull String getOutputMask();

    @GET("{@network}")
    @NotNull NetworkCall<InputOutputNetwork> getNetwork();

    @POST("/rw/iosystem/signals?action=signal-search")
    @NotNull NetworkCall<List<InputOutputSignal>> getSignals(
            @Field("network") String network,
            @Field("device") String device
    );

    default @NotNull NetworkCall<List<InputOutputSignal>> getSignals() {
        String network = getTitle().substring(0, getTitle().lastIndexOf('/'));
        String device = getTitle().substring(getTitle().lastIndexOf('/') + 1);
        return getSignals(network, device);
    }

    @POST("{@self}?action=set")
    @NotNull NetworkCall<Void> setState(
            @NotNull InputOutputLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkCall<InputOutputNetworkEvent> onState();

    @POST("{@self}?action=set-inputdata")
    @NotNull NetworkCall<Void> setInputData(
            @Field("startbyte") int index,
            @Field("signaldata") byte data,
            @Field("datamask") byte mask
    );


    @POST("{@self}?action=set-outputdata")
    @NotNull NetworkCall<Void> setOutputData(
            @Field("startbyte") int index,
            @Field("signaldata") byte data,
            @Field("datamask") byte mask
    );
}
