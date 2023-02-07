package com.bossymr.rapid.robot.network;

import com.bossymr.network.EntityModel;
import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-hdtr-ev")
public interface HoldToRunEvent extends EntityModel {

    @Property("hdtr-State")
    @NotNull HoldToRunState getState();

}
