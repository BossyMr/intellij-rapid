package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;

public interface VirtualSymbol extends RapidSymbol {

    @Override
    @NotNull String getName();

    @Override
    default @NotNull Pointer<? extends VirtualSymbol> createPointer() {
        return new VirtualPointer(getName());
    }
}
