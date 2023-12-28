package com.bossymr.rapid.ide.search;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.api.UsageHandler;
import com.intellij.model.Pointer;
import com.intellij.platform.backend.presentation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class RapidSymbolSearchTarget implements SearchTarget {

    private final @NotNull RapidSymbol symbol;

    public RapidSymbolSearchTarget(@NotNull RapidSymbol symbol) {
        this.symbol = symbol;
    }

    public @NotNull RapidSymbol getSymbol() {
        return symbol;
    }

    @Override
    public @NotNull UsageHandler getUsageHandler() {
        return UsageHandler.createEmptyUsageHandler(symbol.getPresentableName());
    }

    @Override
    public @NotNull Pointer<RapidSymbolSearchTarget> createPointer() {
        return Pointer.delegatingPointer(symbol.createPointer(), RapidSymbolSearchTarget::new);
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        return symbol.getTargetPresentation();
    }
}
