package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Property;

public interface RoutineModel extends SymbolModel, VisibleSymbol {

    @Property("npar")
    int getParameterCount();
}
