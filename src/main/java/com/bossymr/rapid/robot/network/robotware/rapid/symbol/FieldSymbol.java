package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

public interface FieldSymbol extends Symbol, VisibleSymbol {

    @Property("typurl")
    @Nullable String getCanonicalType();

    @Property("dattyp")
    @Nullable String getDataType();

    @Property("ndim")
    int getSize();
}
