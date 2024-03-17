package com.bossymr.rapid.language.symbol.resolve;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalModule;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalVisibleSymbol;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of {@code PsiScopeProcessor} used to collect declarations when resolving references.
 */
public class ResolveScopeProcessor implements Processor<RapidSymbol> {

    protected final @NotNull List<RapidSymbol> symbols;

    private final @NotNull PsiElement context;
    private final @Nullable PhysicalModule module;
    private final @Nullable Module task;

    private final @Nullable String name;

    /**
     * Creates a new {@code ResolveScopeProcessor}.
     *
     * @param context the element to use as reference when calculating if an element is reachable.
     * @param name the name of the declaration, or {@code null} to collect all declarations.
     */
    public ResolveScopeProcessor(@NotNull PsiElement context, @Nullable String name) {
        this.symbols = new ArrayList<>();
        this.context = context;
        this.module = PhysicalModule.getModule(context);
        this.task = ModuleUtil.findModuleForPsiElement(context);
        this.name = name;
    }

    public @NotNull PsiElement getContext() {
        return context;
    }

    public @Nullable String getName() {
        return name;
    }

    @Override
    public boolean process(@NotNull RapidSymbol symbol) {
        if (name == null) {
            if (!(symbols.contains(symbol))) {
                symbols.add(symbol);
            }
            return true;
        }
        if (name.equalsIgnoreCase(symbol.getName()) && isAccessible(symbol)) {
            if (!(symbols.contains(symbol))) {
                symbols.add(symbol);
            }
            return false;
        }
        return true;
    }

    private boolean isAccessible(@NotNull RapidSymbol symbol) {
        if (!(symbol instanceof PhysicalSymbol physicalSymbol)) {
            // Virtual symbols are always accessible.
            return true;
        }
        if (!(symbol instanceof PhysicalVisibleSymbol visibleSymbol)) {
            // Symbols which aren't routine or field or record declarations won't be processed unless they already are reachable.
            return true;
        }
        if (module == null || module.equals(PhysicalModule.getModule(physicalSymbol))) {
            return true;
        }
        if (visibleSymbol.getVisibility() == Visibility.LOCAL) {
            return false;
        }
        if (task == null || task.equals(ModuleUtil.findModuleForPsiElement(physicalSymbol))) {
            return true;
        }
        return visibleSymbol.getVisibility() != Visibility.TASK;
    }

    public @NotNull List<RapidSymbol> getSymbols() {
        return symbols;
    }
}
