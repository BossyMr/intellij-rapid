package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Fetch;
import org.jetbrains.annotations.NotNull;

@Entity("user-rmmp-ev")
public interface ManualModePrivilegeEvent {

        @Fetch("{@resource}")
  @NotNull NetworkQuery<ManualModePrivilegeState> getState();

}
