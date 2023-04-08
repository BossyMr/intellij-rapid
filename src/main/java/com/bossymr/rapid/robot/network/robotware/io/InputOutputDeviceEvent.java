package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ios-devicestate-ev")
public interface InputOutputDeviceEvent {

    @Property("pstate")
    @NotNull InputOutputPhysicalState getPhysicalState();

    @Property("lstate")
    @NotNull InputOutputLogicalState getLogicalState();

}
