package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.annotations.Field;
import com.bossymr.rapid.robot.network.annotations.GET;
import com.bossymr.rapid.robot.network.annotations.POST;
import com.bossymr.rapid.robot.network.annotations.Service;
import com.bossymr.rapid.robot.network.query.Query;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A service used to access the I/O system of a robot, including connected networks, devices, and signals.
 */
@Service("/rw/iosystem")
public interface InputOutputService {

    @GET("/networks/{network}")
    @NotNull Query<InputOutputNetwork> getNetwork(
            @NotNull @Field("network") String network
    );

    @GET("/networks")
    @NotNull Query<List<InputOutputNetwork>> getNetworks();

    @GET("/devices")
    @NotNull Query<List<InputOutputDevice>> getDevices();

    @GET("/devices/{device}")
    @NotNull Query<InputOutputNetwork> getDevices(
            @NotNull @Field("device") String device
    );

    @GET("/signals")
    @NotNull Query<List<InputOutputSignal>> getSignals();

    @GET("/signals/{network}/{device}/{signal}")
    @NotNull Query<InputOutputSignal> getSignal(
            @NotNull @Field("network") String network,
            @NotNull @Field("device") String device,
            @NotNull @Field("signal") String signal
    );

    @POST("/signals?action=unblock-signals")
    @NotNull Query<Void> unblockSignals();
}
