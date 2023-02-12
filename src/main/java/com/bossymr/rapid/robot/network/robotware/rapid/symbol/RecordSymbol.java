package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.NotNull;

@Entity({"rap-symproprecord", "rap-symproprecord-li"})
public interface RecordSymbol extends Symbol, VisibleSymbol {

    @Property("ncom")
    int getComponentCount();

    @Property("valtyp")
    @NotNull String getValueType();

}
