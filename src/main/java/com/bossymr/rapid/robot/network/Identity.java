package com.bossymr.rapid.robot.network;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("ctrl-identity-info")
public interface Identity {

    @Property("ctrl-name")
    @NotNull String getName();

}
