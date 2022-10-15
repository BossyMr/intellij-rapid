package com.bossymr.rapid.language.psi.light;

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.util.IncorrectOperationException;
import com.bossymr.rapid.language.psi.RapidSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class LightSymbol extends FakePsiElement implements RapidSymbol {

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public @Nullable PsiElement getNameIdentifier() {
        return null;
    }

    @Override
    public abstract @NotNull String getName();

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("Cannot rename virtual symbol '" + getName() + "'");
    }

    @Override
    public PsiElement getParent() {
        return null;
    }
}
