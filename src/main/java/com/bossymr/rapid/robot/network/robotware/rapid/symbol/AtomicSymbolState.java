package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

/**
 * An {@code AtomicSymbol} represents an atomic structure.
 */
@Entity({"rap-sympropatomic", "rap-sympropatomic-li"})
public interface AtomicSymbolState extends SymbolState {

    /**
     * Returns the size of the representation of this atomic in memory.
     *
     * @return the size of this atomic.
     */
    @Property("size")
    int getSize();

    /**
     * Returns the value type of this atomic.
     *
     * @return the value type of this atomic.
     */
    @Property("valtyp")
    @Nullable String getValueType();

    /**
     * Returns the canonical name of the type of this atomic.
     *
     * @return the canonical name of the type of this atomic.
     */
    @Property("typurl")
    @Nullable String getCanonicalType();

    /**
     * Returns the type of this atomic.
     *
     * @return the type of this atomic.
     */
    @Property("dattyp")
    @Nullable String getDataType();

}
