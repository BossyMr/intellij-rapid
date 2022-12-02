package com.bossymr.rapid.language.symbol;

import com.intellij.model.Pointer;
import com.intellij.model.Symbol;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface RapidSymbol extends Symbol {

    @Nullable String getName();

    @Override
    @NotNull Pointer<? extends RapidSymbol> createPointer();

    @NotNull TargetPresentation getTargetPresentation();

    @Override
    boolean equals(Object obj);

    @Override
    int hashCode();
}
