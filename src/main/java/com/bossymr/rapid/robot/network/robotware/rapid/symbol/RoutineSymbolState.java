package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;

public interface RoutineSymbolState extends SymbolState, VisibleSymbolState {

    @Property("npar")
    int getParameterCount();
}
