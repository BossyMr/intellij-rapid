package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Entity({"rap-sympropparam", "rap-sympropparam-li"})
public interface ParameterSymbolState extends SymbolState, QueryableSymbolState {

    /**
     * Returns the index of this parameter in its mutually exclusive parameter group.
     *
     * @return the index (starting at 1) of this parameter in its parameter group.
     */
    @Property("altnum")
    int getGroupIndex();

    @Property("dattyp")
    @Nullable String getDataType();

    @Property("mode")
    @NotNull String getMode();

    @Property("ndim")
    int getSize();

    /**
     * Returns the index of this parameter in its parameter list. Multiple parameters can share the same index, if they
     * are mutually exclusive, in that case {@link #getGroupIndex()} can be used to order mutually exlusive parameters.
     *
     * @return the index (starting at 1) of this parameter in its parameter list.
     */
    @Property("parnum")
    int getIndex();

    @Property("required")
    boolean isRequired();

    @Property("typurl")
    @NotNull String getCanonicalType();

}
