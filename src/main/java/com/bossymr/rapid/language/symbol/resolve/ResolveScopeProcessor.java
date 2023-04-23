package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.RapidVisibleSymbol;
import com.bossymr.rapid.language.symbol.SymbolUtil;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResolveScopeProcessor implements Processor<RapidSymbol> {

    private final List<RapidSymbol> symbols;

    private final PsiElement context;
    private final String name;

    public ResolveScopeProcessor(@NotNull PsiElement context, @Nullable String name) {
        this.symbols = new ArrayList<>();
        this.context = context;
        this.name = name;
    }

    public @Nullable String getName() {
        return name;
    }

    public @NotNull PsiElement getContext() {
        return context;
    }

    @Override
    public boolean process(@NotNull RapidSymbol symbol) {
        if (symbols.contains(symbol)) {
            throw new IllegalArgumentException();
        }
        if (this.name != null && !(this.name.equalsIgnoreCase(symbol.getName()))) {
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
                if (symbol instanceof RapidVisibleSymbol accessibleSymbol) {
                    return switch (accessibleSymbol.getVisibility()) {
                        case LOCAL -> false;
                        case TASK, GLOBAL -> true;
                    };
                }
            }
        }
        return true;
    }

    public @NotNull List<RapidSymbol> getSymbols() {
        return symbols;
    }

}
