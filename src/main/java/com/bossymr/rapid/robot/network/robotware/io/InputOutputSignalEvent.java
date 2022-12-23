package com.bossymr.rapid.robot.network.robotware.io;

import com.bossymr.rapid.robot.network.EntityModel;
import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ios-signalstate-ev")
public interface InputOutputSignalEvent extends EntityModel {

    @Property("pvalue")
    byte getLogicalValue();

    @Property("lstate")
    @NotNull InputOutputSignalLogicalState getLogicalState();

}
