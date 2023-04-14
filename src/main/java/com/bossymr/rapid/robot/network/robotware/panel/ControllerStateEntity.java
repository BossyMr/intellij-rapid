package com.bossymr.rapid.robot.network.robotware.panel;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity({"pnl-ctrlstate", "pnl-ctrlstate-ev"})
public interface ControllerStateEntity {

    @Property("ctrlstate")
    @NotNull ControllerState getState();

}
