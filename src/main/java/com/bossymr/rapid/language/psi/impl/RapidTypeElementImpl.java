package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.psi.RapidTypeElement;
import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
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
        return CachedValuesManager.getProjectPsiDependentCache(this, (ignored) -> {
            RapidReferenceExpression expression = getReferenceExpression();
            if (expression != null) {
                PsiElement element = expression.resolve();
                return new RapidType(element instanceof RapidStructure ? (RapidStructure) element : null);
            } else {
                return null;
            }
        });
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
