package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Field;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.POST;
import com.bossymr.network.annotations.Service;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A service used to access the I/O system of a robot, including connected networks, devices, and signals.
 */
@Service("/rw/iosystem")
public interface InputOutputService {

    @GET("/networks/{network}")
    @NotNull NetworkCall<InputOutputNetwork> getNetwork(
            @NotNull @Field("network") String network
    );

    @GET("/networks")
    @NotNull NetworkCall<List<InputOutputNetwork>> getNetworks();

    @GET("/devices")
    @NotNull NetworkCall<List<InputOutputDevice>> getDevices();

    @GET("/devices/{device}")
    @NotNull NetworkCall<InputOutputNetwork> getDevices(
            @NotNull @Field("device") String device
    );

    @GET("/signals")
    @NotNull NetworkCall<List<InputOutputSignal>> getSignals();

    @GET("/signals/{network}/{device}/{signal}")
    @NotNull NetworkCall<InputOutputSignal> getSignal(
            @NotNull @Field("network") String network,
            @NotNull @Field("device") String device,
            @NotNull @Field("signal") String signal
    );

    @POST("/signals?action=unblock-signals")
    @NotNull NetworkCall<Void> unblockSignals();
}
