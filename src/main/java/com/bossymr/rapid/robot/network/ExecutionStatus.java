package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-execution")
public interface ExecutionStatus extends EntityModel {

    @Property("ctrlexecstate")
    @NotNull ExecutionState getState();

    @Property("cycle")
    @NotNull ExecutionCycle getCycle();

}
