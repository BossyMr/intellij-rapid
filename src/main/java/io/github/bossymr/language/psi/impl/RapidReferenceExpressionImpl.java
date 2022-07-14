package io.github.bossymr.language.psi.impl;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidReferenceExpressionImpl extends RapidExpressionElement implements RapidReferenceExpression {

    public RapidReferenceExpressionImpl() {
        super(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public @Nullable RapidType getType() {
        return null;
    }

    @Override
    public @Nullable RapidExpression getQualifier() {
        return null;
    }

    @Override
    public @Nullable String getReferenceName() {
        return null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {

    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return new ResolveResult[0];
    }

    @Override
    public @NotNull PsiElement getElement() {
        return null;
    }

    @Override
    public @NotNull TextRange getRangeInElement() {
        return null;
    }

    @Override
    public @Nullable PsiElement resolve() {
        return null;
    }

    @Override
    public @NotNull @NlsSafe String getCanonicalText() {
        return null;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        return null;
    }

    @Override
    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
        return null;
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return false;
    }

    @Override
    public boolean isSoft() {
        return false;
    }
}
