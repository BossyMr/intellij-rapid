package com.bossymr.rapid.language.symbol.virtual;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.Pointer;
import org.jetbrains.annotations.NotNull;

public interface VirtualSymbol extends RapidSymbol {

    @Override
    default @NotNull Pointer<VirtualSymbol> createPointer() {
        return new VirtualPointer(this);
    }
}
