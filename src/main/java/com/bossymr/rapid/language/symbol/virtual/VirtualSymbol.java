package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import org.jetbrains.annotations.NotNull;

public interface VirtualSymbol extends RapidSymbol {

    @Override
    @NotNull String getName();

}
