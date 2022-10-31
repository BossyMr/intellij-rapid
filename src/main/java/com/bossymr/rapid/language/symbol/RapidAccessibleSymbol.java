package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

public interface RapidAccessibleSymbol extends RapidSymbol {

    @NotNull Visibility getVisibility();

}
