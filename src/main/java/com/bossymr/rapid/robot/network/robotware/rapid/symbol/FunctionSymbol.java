package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-sympropfunction", "rap-sympropfunction-li"})
public interface FunctionSymbol extends Symbol, VisibleSymbol, RoutineSymbol {

    @Property("linked")
    boolean isLinked();

    @Property("typurl")
    @NotNull String getCanonicalType();

    @Property("dattyp")
    @NotNull String getDataType();

}
