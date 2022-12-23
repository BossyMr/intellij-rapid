package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.POST;
import com.bossymr.rapid.robot.network.annotations.Property;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
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
    @NotNull Query<InputOutputDevice> getDevice();

    @POST("{@self}?action=set")
    @NotNull Query<Void> setState(
            @NotNull InputOutputSignalLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableQuery<InputOutputSignalEvent> onState();
}
