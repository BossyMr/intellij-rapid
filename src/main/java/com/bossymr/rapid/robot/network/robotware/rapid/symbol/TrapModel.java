package com.bossymr.rapid.robot.network.robotware.rapid.symbol;

import com.bossymr.network.annotations.Entity;

/**
 * A {@code TrapSymbol} represents a {@code TRAP} routine.
 */
@Entity({"rap-symproptrap", "rap-symproptrap-li"})
public interface TrapModel extends SymbolModel, VisibleSymbol, RoutineModel {}
