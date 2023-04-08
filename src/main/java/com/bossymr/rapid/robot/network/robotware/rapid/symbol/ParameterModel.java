package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-sympropparam")
public interface ParameterModel extends QueryableSymbol {

    /**
     * Returns the index of this parameter in its mutually exclusive parameter group.
     *
     * @return the index (starting at 1) of this parameter in its parameter group.
     */
    @Property("altnum")
    int getGroupIndex();

    @Property("mode")
    @NotNull String getMode();

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

}
