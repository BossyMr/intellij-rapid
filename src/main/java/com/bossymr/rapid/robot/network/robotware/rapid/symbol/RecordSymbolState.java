package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-symproprecord", "rap-symproprecord-li"})
public interface RecordSymbolState extends SymbolState, VisibleSymbolState {

    @Property("ncom")
    int getComponentCount();

    @Property("valtyp")
    @NotNull String getValueType();

}
