package com.bossymr.rapid.language.symbol;

import com.intellij.model.Symbol;
import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.Nullable;

public interface RapidSymbol extends Symbol {
    @Nullable String getName();

    @Nullable ItemPresentation getPresentation();
}
