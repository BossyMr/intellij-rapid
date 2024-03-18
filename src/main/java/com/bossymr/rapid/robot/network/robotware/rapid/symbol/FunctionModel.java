package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-sympropfunction")
public interface FunctionModel extends RoutineModel {

    @Property("linked")
    boolean isLinked();

    @Property("typurl")
    @NotNull String getCanonicalType();

    @Property("dattyp")
    @NotNull String getDataType();

}
