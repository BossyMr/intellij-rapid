package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.query.Query;
import com.bossymr.rapid.robot.network.query.Query.GET;
import org.jetbrains.annotations.NotNull;

@Entity("user-rmmp-ev")
public interface ManualModePrivilegeEvent extends EntityModel {

    @GET("{@resource}")
    @NotNull Query<ManualModePrivilegeState> getState();

}
