package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.*;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.SubscribableQuery;
import com.bossymr.rapid.robot.network.query.SubscribableQuery.Subscribable;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpRequest;
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
    @NotNull Query<InputOutputNetwork> getNetwork();

    default @NotNull Query<List<InputOutputSignal>> getSignals() {
        String network = getTitle().substring(0, getTitle().lastIndexOf('/'));
        String device = getTitle().substring(getTitle().lastIndexOf('/') + 1);
        HttpRequest httpRequest = HttpRequest.newBuilder(getNetworkClient().getDefaultPath().resolve("/rw/iosystem/signals?action=signal-search"))
                .POST(HttpRequest.BodyPublishers.ofString("network=" + network + "&device=" + device))
                .build();
        return getNetworkClient().newQuery(httpRequest, InputOutputSignal.class);
    }

    @POST("{@self}?action=set")
    @NotNull Query<Void> setState(
            @NotNull InputOutputLogicalState logicalState
    );

    @Subscribable("{@self};state")
    @NotNull SubscribableQuery<InputOutputNetworkEvent> onState();

    @POST("{@self}?action=set-inputdata")
    @NotNull Query<Void> setInputData(
            @Field("startbyte") int index,
            @Field("signaldata") byte data,
            @Field("datamask") byte mask
    );


    @POST("{@self}?action=set-outputdata")
    @NotNull Query<Void> setOutputData(
            @Field("startbyte") int index,
            @Field("signaldata") byte data,
            @Field("datamask") byte mask
    );
}
