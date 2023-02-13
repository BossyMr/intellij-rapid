package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;

@Entity({"rap-sympropconstant", "rap-sympropconstant-li"})
public interface ConstantSymbolState extends SymbolState, VisibleSymbolState, FieldSymbolState {

    @Property("linked")
    boolean isLinked();

}
