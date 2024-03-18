package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-data")
public interface SymbolValue {

    @Property("value")
    @NotNull String getValue();

}
