package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.SubscribableNetworkCall;
import com.bossymr.network.annotations.*;
import org.jetbrains.annotations.NotNull;

@Entity({"ios-signal-li", "ios-signal"})
public interface InputOutputSignal extends EntityModel {

    @Property("name")
    @NotNull String getName();

    @Property("type")
    @NotNull InputOutputSignalType getSignalType();

    @Property("category")
    @NotNull String getCategory();

    @Property("lvalue")
    byte getLogicalValue();

    @Property("lstate")
    @NotNull InputOutputSignalLogicalState getLogicalState();

    @Property("unitm")
    @NotNull String getDeviceName();

    @Property("phstate")
    @NotNull InputOutputSignalPhysicalState getPhysicalState();

    @Property("pvalue")
    byte getPhysicalValue();

    @Property("ltime-sec")
    int getLogicalSecondTime();

    @Property("ltime-microsec")
    int getLogicalMicroSecondTime();

    @Property("ptime-sec")
    int getPhysicalSecondTime();

    @Property("ptime-microsec")
    int getPhysicalMicroSecondTime();

    @Property("quality")
    int getSignalQuality();

    @GET("{@device}")
    @NotNull NetworkCall<InputOutputDevice> getDevice();

    @POST("{@self}?action=set")
    @NotNull NetworkCall<Void> setState(
            @NotNull InputOutputSignalLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkCall<InputOutputSignalEvent> onState();
}
