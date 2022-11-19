package com.bossymr.rapid.language.symbol;

import com.intellij.navigation.ItemPresentation;
import org.jetbrains.annotations.Nullable;

public interface RapidSymbol {
    @Nullable String getName();

    @Nullable ItemPresentation getPresentation();
}
