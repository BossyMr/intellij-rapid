package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;

@Entity("rap-sympropconstant")
public interface ConstantModel extends SymbolModel, VisibleSymbol, FieldModel {

    @Property("linked")
    boolean isLinked();

}
