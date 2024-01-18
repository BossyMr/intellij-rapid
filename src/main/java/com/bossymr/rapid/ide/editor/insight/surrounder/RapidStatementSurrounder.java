package com.bossymr.rapid.ide.editor.insight.surrounder;

import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public abstract class RapidStatementSurrounder implements Surrounder {

    @Override
    public boolean isApplicable(PsiElement @NotNull [] elements) {
        return true;
    }
}
