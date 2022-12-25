package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.symbol.RapidAccessibleSymbol;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class RapidScopeProcessor implements Processor<RapidSymbol> {

    private final Set<RapidSymbol> symbols;
    private final PsiElement context;
    private final String name;

    public RapidScopeProcessor(@NotNull PsiElement context, @NotNull String name) {
        this.symbols = new HashSet<>();
        this.context = context;
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public boolean process(@NotNull RapidSymbol symbol) {
        if (!this.name.equalsIgnoreCase(symbol.getName())) {
            return true;
        }
        if (isAccessible(symbol)) {
            symbols.add(symbol);
            return false;
        }
        return true;
    }

    private boolean isAccessible(@NotNull RapidSymbol symbol) {
        if (symbol instanceof PhysicalSymbol physical) {
            if (Objects.equals(SymbolUtil.getModule(physical), SymbolUtil.getModule(context))) {
                return true;
            } else {
                if (symbol instanceof RapidAccessibleSymbol accessibleSymbol) {
                    return switch (accessibleSymbol.getVisibility()) {
                        case LOCAL -> false;
                        case TASK, GLOBAL -> true;
                    };
                }
            }
        }
        return true;
    }

    public @NotNull Set<RapidSymbol> getSymbols() {
        return symbols;
    }
}
