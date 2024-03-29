package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code AliasSymbol} represent an alias structure.
 */
@Entity("rap-sympropalias")
public interface AliasModel extends VisibleSymbol {

    @Property("linked")
    boolean isLinked();

    @Property("typurl")
    @Nullable String getCanonicalType();

    @Property("dattyp")
    @Nullable String getDataType();
}
