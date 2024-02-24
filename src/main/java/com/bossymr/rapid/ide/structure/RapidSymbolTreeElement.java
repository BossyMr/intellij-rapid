package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.RapidVisibleSymbol;
import com.bossymr.rapid.language.symbol.Visibility;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RapidSymbolTreeElement<T extends PsiElement & RapidVisibleSymbol> extends PsiTreeElementBase<T> {

    protected RapidSymbolTreeElement(@Nullable T element) {
        super(element);
    }

    public @NotNull Visibility getVisibility() {
        return getValue() != null ? getValue().getVisibility() : Visibility.GLOBAL;
    }
}
