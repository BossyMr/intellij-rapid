package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public interface RapidReferenceExpression extends RapidExpression, PsiPolyVariantReference {

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

    @NotNull List<RapidSymbol> getSymbols();

    @Nullable RapidSymbol getSymbol();

    @Override
    default ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        Collection<RapidSymbol> symbols = getSymbols();
        return symbols.stream()
                .filter(symbol -> symbol instanceof PsiElement)
                .map(symbol -> new PsiElementResolveResult((PsiElement) symbol))
                .toArray(ResolveResult[]::new);
    }

    @Override
    @Nullable
    default PsiElement resolve() {
        RapidSymbol symbol = getSymbol();
        return symbol instanceof PsiElement ? (PsiElement) symbol : null;
    }
}
