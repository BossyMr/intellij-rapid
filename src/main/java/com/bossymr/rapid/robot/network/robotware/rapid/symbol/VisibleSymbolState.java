package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;

public interface VisibleSymbolState extends SymbolState {

    @Property("local")
    boolean isLocal();
}
