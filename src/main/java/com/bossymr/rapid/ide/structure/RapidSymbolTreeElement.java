package com.bossymr.rapid.ide.structure;

import com.bossymr.rapid.language.symbol.RapidVisibleSymbol;
import com.bossymr.rapid.language.symbol.Visibility;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.structureView.impl.java.AccessLevelProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RapidSymbolTreeElement<T extends PsiElement & RapidVisibleSymbol> extends PsiTreeElementBase<T> implements AccessLevelProvider {

    protected RapidSymbolTreeElement(@Nullable T element) {
        super(element);
    }

    public @NotNull Visibility getVisibility() {
        return getValue() != null ? getValue().getVisibility() : Visibility.GLOBAL;
    }

    @Override
    public int getAccessLevel() {
        return switch (getVisibility()) {
            case LOCAL -> PsiUtil.ACCESS_LEVEL_PRIVATE;
            case TASK -> PsiUtil.ACCESS_LEVEL_PACKAGE_LOCAL;
            case GLOBAL -> PsiUtil.ACCESS_LEVEL_PUBLIC;
        };
    }

    @Override
    public int getSubLevel() {
        return 0;
    }
}
