package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.NetworkCall;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.GET;
import org.jetbrains.annotations.NotNull;

@Entity("user-rmmp-ev")
public interface ManualModePrivilegeEvent extends EntityModel {

    @GET("{@resource}")
    @NotNull NetworkCall<ManualModePrivilegeState> getState();

}
