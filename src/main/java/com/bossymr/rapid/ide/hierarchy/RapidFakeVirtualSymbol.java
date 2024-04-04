package com.bossymr.rapid.ide.hierarchy;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.model.Pointer;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.impl.source.DummyHolder;
import com.intellij.psi.impl.source.DummyHolderFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidFakeVirtualSymbol extends FakePsiElement {

    private final @NotNull PsiManager manager;
    private final @NotNull DummyHolder dummyHolder;
    private final @NotNull RapidSymbol symbol;

    public RapidFakeVirtualSymbol(@NotNull PsiManager manager, @NotNull RapidSymbol symbol) {
        this.manager = manager;
        this.symbol = symbol;
        this.dummyHolder = DummyHolderFactory.createHolder(manager, null);
    }

    public @NotNull RapidSymbol getSymbol() {
        return symbol;
    }

    @Override
    public @NotNull PsiManager getManager() {
        return manager;
    }

    @Override
    public @Nullable PsiElement getParent() {
        return dummyHolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidFakeVirtualSymbol that = (RapidFakeVirtualSymbol) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }
}
