package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface RapidReferenceExpression extends RapidExpression, PsiSymbolReference {

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

    @NotNull String getCanonicalText();

    @Nullable RapidSymbol resolve();

    @Override
    @NotNull Collection<RapidSymbol> resolveReference();

    @Override
    default @NotNull Collection<RapidReferenceExpression> getOwnReferences() {
        return List.of(this);
    }
}
