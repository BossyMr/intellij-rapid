package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;

public interface VisibleSymbol extends SymbolModel {

    @Property("local")
    boolean isLocal();
}
