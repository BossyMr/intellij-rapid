package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;

@Entity({"rap-sympropconstant", "rap-sympropconstant-li"})
public interface ConstantModel extends SymbolModel, VisibleSymbol, FieldModel {

    @Property("linked")
    boolean isLinked();

}
