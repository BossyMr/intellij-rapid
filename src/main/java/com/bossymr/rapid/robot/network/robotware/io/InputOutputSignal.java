package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.SubscribableNetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import com.bossymr.rapid.robot.api.annotations.Property;
import com.bossymr.rapid.robot.api.annotations.Subscribable;
import com.bossymr.rapid.robot.api.client.FetchMethod;
import org.jetbrains.annotations.NotNull;

@Entity({"ios-signal-li", "ios-signal"})
public interface InputOutputSignal {

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

        @Fetch("{@device}")
  @NotNull NetworkQuery<InputOutputDevice> getDevice();

    @Fetch(method = FetchMethod.POST, value = "{@self}?action=set")
  @NotNull NetworkQuery<Void> setState(@NotNull InputOutputSignalLogicalState logicalState);

    @Subscribable("{@self};state")
    @NotNull SubscribableNetworkQuery<InputOutputSignalEvent> onState();
}
