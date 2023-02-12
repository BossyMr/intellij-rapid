package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Property;

public interface VisibleSymbol extends Symbol {

    @Property("local")
    boolean isLocal();
}
