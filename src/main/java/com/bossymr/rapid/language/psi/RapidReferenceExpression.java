package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public interface RapidReferenceExpression extends RapidExpression, PsiSymbolReference, PsiPolyVariantReference {

    /**
     * Returns the qualifier of the reference (the content before the period).
     *
     * @return the qualifier, or {@code null} if the reference is not qualified.
     */
    @Nullable RapidExpression getQualifier();

    /**
     * Returns the name of the reference.
     *
     * @return the name of the reference, or {@code null} if the reference is not complete.
     */
    @Nullable PsiElement getIdentifier();

    @Override
    default @NotNull PsiElement getElement() {
        return this;
    }

    @Override
    default @NotNull TextRange getAbsoluteRange() {
        return PsiSymbolReference.super.getAbsoluteRange();
    }

    @Override
    default @NotNull List<RapidSymbol> resolveReference() {
        return getSymbols();
    }

    @NotNull List<RapidSymbol> getSymbols();

    @Nullable RapidSymbol getSymbol();
}
