package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;

public interface RoutineSymbol extends Symbol, VisibleSymbol {

    @Property("npar")
    int getParameterCount();
}
