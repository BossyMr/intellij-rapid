package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-ctrlexecstate-ev")
public interface ExecutionStateEvent extends EntityModel {

    @Property("ctrlexecstate")
    @NotNull ExecutionState getState();

}
