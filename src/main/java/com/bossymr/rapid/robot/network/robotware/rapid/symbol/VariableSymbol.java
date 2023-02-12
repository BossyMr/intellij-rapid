package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity({"rap-sympropvar", "rap-sympropvar-li"})
public interface VariableSymbol extends Symbol, VisibleSymbol, FieldSymbol {

    @Property("dim")
    @Nullable String getDimensions();

    @Property("rdonly")
    boolean isReadOnly();

    @Property("taskvar")
    boolean isTaskVariable();

}
