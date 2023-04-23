package com.bossymr.rapid.language.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * A standard implementation for a {@link RapidElement}.
 */
public abstract class PhysicalElement extends ASTWrapperPsiElement implements RapidElement {

    protected PhysicalElement(@NotNull ASTNode node) {
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
