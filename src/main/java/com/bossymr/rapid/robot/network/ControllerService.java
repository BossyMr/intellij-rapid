package com.bossymr.rapid.robot.network;

import com.bossymr.network.NetworkCall;
import com.bossymr.network.ServiceModel;
import com.bossymr.network.annotations.GET;
import com.bossymr.network.annotations.Service;
import org.jetbrains.annotations.NotNull;

@Service("/ctrl")
public interface ControllerService extends ServiceModel {

    @GET("/identity")
    @NotNull NetworkCall<Identity> getIdentity();

}
