package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.Nullable;

public interface RapidComponent extends RapidSymbol {

    @Nullable RapidType getType();

}
