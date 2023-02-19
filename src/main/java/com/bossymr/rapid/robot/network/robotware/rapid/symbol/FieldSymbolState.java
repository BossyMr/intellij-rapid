package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

public interface FieldSymbolState extends SymbolState, VisibleSymbolState, QueryableSymbolState {

    @Property("typurl")
    @Nullable String getCanonicalType();

    @Property("dattyp")
    @Nullable String getDataType();

    @Property("ndim")
    int getSize();
}
