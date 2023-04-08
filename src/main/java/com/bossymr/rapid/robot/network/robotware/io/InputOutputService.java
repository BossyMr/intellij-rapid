package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.annotations.Fetch;
import com.bossymr.network.annotations.FetchMethod;
import com.bossymr.network.annotations.Field;
import com.bossymr.network.annotations.Service;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A service used to access the I/O system of a robot, including connected networks, devices, and signals.
 */
@Service("/rw/iosystem")
public interface InputOutputService {

    @Fetch("/networks/{network}")
    @NotNull InputOutputNetwork getNetwork(
            @NotNull @Field("network") String network
    );

    @Fetch("/networks")
    @NotNull List<InputOutputNetwork> getNetworks();

    @Fetch("/devices")
    @NotNull List<InputOutputDevice> getDevices();

    @Fetch("/devices/{device}")
    @NotNull InputOutputNetwork getDevices(
            @NotNull @Field("device") String device
    );

    @Fetch("/signals")
    @NotNull List<InputOutputSignal> getSignals();

    @Fetch("/signals/{network}/{device}/{signal}")
    @NotNull InputOutputSignal getSignal(
            @NotNull @Field("network") String network,
            @NotNull @Field("device") String device,
            @NotNull @Field("signal") String signal
    );

    @Fetch(method = FetchMethod.POST, value = "/signals?action=unblock-signals")
    @NotNull Void unblockSignals();
}
