package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.api.NetworkQuery;
import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Fetch;
import org.jetbrains.annotations.NotNull;

@Entity("user-rmmp-ev")
public interface ManualModePrivilegeEvent {

    @Fetch("{@resource}")
    @NotNull NetworkQuery<ManualModePrivilegeState> getState();

}
