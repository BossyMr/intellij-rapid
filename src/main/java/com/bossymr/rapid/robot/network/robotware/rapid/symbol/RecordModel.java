package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity("rap-symproprecord")
public interface RecordModel extends SymbolModel, VisibleSymbol {

    @Property("ncom")
    int getComponentCount();

    @Property("valtyp")
    @NotNull String getValueType();

}
