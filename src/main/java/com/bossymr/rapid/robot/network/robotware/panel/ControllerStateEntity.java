package com.bossymr.rapid.robot.network.robotware.panel;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity({"pnl-ctrlstate", "pnl-ctrlstate-ev"})
public interface ControllerStateEntity {

    @Property("ctrlstate")
    @NotNull ControllerState getState();

}
