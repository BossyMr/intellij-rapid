package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.rapid.robot.api.annotations.Entity;
import com.bossymr.rapid.robot.api.annotations.Property;
import org.jetbrains.annotations.Nullable;

@Entity("rap-symproppers")
public interface PersistentModel extends SymbolModel, VisibleSymbol, FieldModel {

    @Property("dim")
    @Nullable String getDimensions();

    @Property("rdonly")
    boolean isReadOnly();

    @Property("taskpers")
    boolean isTaskPersistent();

}
