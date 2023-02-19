package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity({"rap-sympropreccomp", "rap-sympropreccomp-li"})
public interface ComponentSymbolState extends SymbolState {

    /**
     * Returns the index of this component in the record. The index of the first component is 1.
     *
     * @return the index (starting at 1) of this component.
     */
    @Property("comnum")
    int getIndex();

    @Property("typurl")
    @Nullable String getCanonicalType();

    @Property("dattyp")
    @Nullable String getDataType();
}
