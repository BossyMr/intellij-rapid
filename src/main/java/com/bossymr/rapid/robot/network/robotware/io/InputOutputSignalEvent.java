package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ios-signalstate-ev")
public interface InputOutputSignalEvent {

    @Property("pvalue")
    byte getLogicalValue();

    @Property("lstate")
    @NotNull InputOutputSignalLogicalState getLogicalState();

}
