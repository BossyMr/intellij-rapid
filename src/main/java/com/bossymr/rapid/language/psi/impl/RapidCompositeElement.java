package com.bossymr.rapid.language.psi.impl;

import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.IElementType;
import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class RapidCompositeElement extends CompositePsiElement implements RapidElement {

    protected RapidCompositeElement(IElementType type) {
        super(type);
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
}
