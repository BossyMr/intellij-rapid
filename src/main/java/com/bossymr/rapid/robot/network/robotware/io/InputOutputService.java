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
  @NotNull NetworkQuery<InputOutputNetwork> getNetwork(@NotNull @Field("network") String network);

        @Fetch("/networks")
  @NotNull NetworkQuery<List<InputOutputNetwork>> getNetworks();

        @Fetch("/devices")
  @NotNull NetworkQuery<List<InputOutputDevice>> getDevices();

        @Fetch("/devices/{device}")
  @NotNull NetworkQuery<InputOutputNetwork> getDevices(@NotNull @Field("device") String device);

        @Fetch("/signals")
  @NotNull NetworkQuery<List<InputOutputSignal>> getSignals();

        @Fetch("/signals/{network}/{device}/{signal}")
  @NotNull NetworkQuery<InputOutputSignal> getSignal(@NotNull @Field("network") String network,
                                              @NotNull @Field("device") String device,
                                              @NotNull @Field("signal") String signal);

    @Fetch(method = FetchMethod.POST, value = "/signals?action=unblock-signals")
  @NotNull NetworkQuery<Void> unblockSignals();
}
