package com.bossymr.rapid.robot.network;

import com.bossymr.rapid.robot.network.annotations.Entity;
import com.bossymr.rapid.robot.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ctrl-identity-info")
public interface Identity extends EntityModel {

    @Property("ctrl-name")
    @NotNull String getName();

}
