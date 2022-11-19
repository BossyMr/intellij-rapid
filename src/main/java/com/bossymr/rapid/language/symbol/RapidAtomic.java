package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.Nullable;

public interface RapidAtomic extends RapidStructure {

    @Nullable RapidType getType();

}
