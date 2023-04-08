package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.SubscribableNetworkQuery;
import com.bossymr.network.annotations.*;
import com.bossymr.network.NetworkQuery;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Entity({"ios-device", "ios-device-li"})
public interface InputOutputDevice {

    @Title
    @NotNull String getTitle();

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

        @Fetch("{@network}")
  @NotNull NetworkQuery<InputOutputNetwork> getNetwork();

        @Fetch(method = FetchMethod.POST, value = "/rw/iosystem/signals?action=signal-search")
  @NotNull NetworkQuery<List<InputOutputSignal>> getSignals(@Field("network") String network,
                                                     @Field("device") String device);

    default @NotNull List<InputOutputSignal> getSignals() {
        String network = getTitle().substring(0, getTitle().lastIndexOf('/'));
        String device = getTitle().substring(getTitle().lastIndexOf('/') + 1);
        return getSignals(network, device);
    }

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=set")
  @NotNull NetworkQuery<Void> setState(@NotNull InputOutputLogicalState logicalState);

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkQuery<InputOutputNetworkEvent> onState();

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=set-inputdata")
  @NotNull NetworkQuery<Void> setInputData(@Field("startbyte") int index,
                                    @Field("signaldata") byte data,
                                    @Field("datamask") byte mask);


    @Fetch(method = FetchMethod.POST, value = "{@self}?action=set-outputdata")
  @NotNull NetworkQuery<Void> setOutputData(@Field("startbyte") int index,
                                     @Field("signaldata") byte data,
                                     @Field("datamask") byte mask);
}
