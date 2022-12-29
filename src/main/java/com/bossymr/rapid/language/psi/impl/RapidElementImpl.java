package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * A standard implementation for a {@link RapidElement}.
 */
public abstract class RapidElementImpl extends ASTWrapperPsiElement implements RapidElement {

    protected RapidElementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof RapidElementVisitor) {
            accept(((RapidElementVisitor) visitor));
        } else {
            visitor.visitElement(this);
        }
    }

    public abstract void accept(@NotNull RapidElementVisitor visitor);

    @Override
    public abstract String toString();
}
