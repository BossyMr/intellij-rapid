package com.bossymr.rapid.ide.editor.insight.unwrap;

import com.intellij.codeInsight.unwrap.AbstractUnwrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public abstract class RapidUnwrapper extends AbstractUnwrapper<RapidUnwrapper.Context> {

    public RapidUnwrapper(@NotNull @Nls String description) {
        super(description);
    }

    @Override
    protected Context createContext() {
        return new Context();
    }

    public void doUnwrap(@NotNull PsiElement element) throws IncorrectOperationException {
        doUnwrap(element, createContext());
    }

    public static class Context extends AbstractUnwrapper.AbstractContext {

        @Override
        protected boolean isWhiteSpace(PsiElement element) {
            return element instanceof PsiWhiteSpace;
        }
    }
}
