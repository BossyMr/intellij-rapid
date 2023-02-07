package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-ctrlexecstate-ev")
public interface ExecutionCycleEvent extends EntityModel {

    @Property("rapidexeccycle")
    @NotNull ExecutionCycle getState();

}
