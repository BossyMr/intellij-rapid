package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

public interface TypeSymbol extends SymbolModel {
    @Property("typurl")
    @NotNull String getCanonicalType();

    @Property("dattyp")
    @NotNull String getDataType();
}
