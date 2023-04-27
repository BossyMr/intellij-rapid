package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

public interface RapidVisibleSymbol extends RapidSymbol {

    @NotNull Visibility getVisibility();

}
