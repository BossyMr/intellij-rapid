package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;
import com.bossymr.network.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity({"rap-symproppers", "rap-symproppers-li"})
public interface PersistentModel extends SymbolModel, VisibleSymbol, FieldModel {

    @Property("dim")
    @Nullable String getDimensions();

    @Property("rdonly")
    boolean isReadOnly();

    @Property("taskpers")
    boolean isTaskPersistent();

}
