package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.Nullable;

public interface RapidVariable extends RapidSymbol {

    @Nullable RapidType getType();

}
