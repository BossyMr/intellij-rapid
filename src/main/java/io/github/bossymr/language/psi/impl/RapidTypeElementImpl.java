package io.github.bossymr.language.psi.impl;

import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import io.github.bossymr.language.psi.*;
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
        return getReferenceExpression() != null ? RapidType.create(getReferenceExpression()) : null;
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
