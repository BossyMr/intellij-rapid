package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-hdtr-ev")
public interface HoldToRunEvent extends EntityModel {

    @Property("hdtr-State")
    @NotNull HoldToRunState getState();

}
