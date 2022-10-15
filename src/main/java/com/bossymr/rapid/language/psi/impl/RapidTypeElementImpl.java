package com.bossymr.rapid.language.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.bossymr.rapid.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidTypeElementImpl extends RapidCompositeElement implements RapidTypeElement {

    public RapidTypeElementImpl() {
        super(RapidElementTypes.TYPE_ELEMENT);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTypeElement(this);
    }

    @Override
    public @Nullable RapidType getType() {
        return CachedValuesManager.getProjectPsiDependentCache(this, this::getType);
    }

    private @Nullable RapidType getType(RapidTypeElement typeElement) {
        RapidReferenceExpression expression = getReferenceExpression();
        if (expression != null) {
            PsiElement element = expression.resolve();
            return new RapidType(element instanceof RapidStructure ? (RapidStructure) element : null);
        } else {
            return null;
        }
    }

    @Override
    public @Nullable RapidReferenceExpression getReferenceExpression() {
        return PsiTreeUtil.findChildOfType(this, RapidReferenceExpression.class);
    }

    @Override
    public String toString() {
        return "RapidTypeElement:" + getText();
    }
}
