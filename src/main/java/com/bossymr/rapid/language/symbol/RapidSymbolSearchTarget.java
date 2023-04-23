package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.RapidBundle;
import com.intellij.find.usages.api.SearchTarget;
import com.intellij.find.usages.api.UsageHandler;
import com.intellij.model.Pointer;
import com.intellij.navigation.TargetPresentation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class RapidSymbolSearchTarget implements SearchTarget {

    private final @NotNull RapidSymbol symbol;

    public RapidSymbolSearchTarget(@NotNull RapidSymbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public @NotNull UsageHandler getUsageHandler() {
        return options -> RapidBundle.message("symbol.search.target.usage.handler", symbol.getPresentableName());
    }

    @Override
    public @NotNull Pointer<? extends SearchTarget> createPointer() {
        return Pointer.delegatingPointer(symbol.createPointer(), RapidSymbolSearchTarget::new);
    }

    @Override
    public @NotNull TargetPresentation presentation() {
        return symbol.getTargetPresentation();
    }
}
