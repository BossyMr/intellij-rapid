package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.Pointer;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface PhysicalSymbol extends RapidSymbol, PsiSymbolDeclaration, RapidElement, PsiNameIdentifierOwner, NavigatablePsiElement {

    @Override
    default @NotNull Collection<PhysicalSymbol> getOwnDeclarations() {
        if (getNameIdentifier() != null) {
            return List.of(this);
        }
        return Collections.emptyList();
    }

    @Override
    default @NotNull PhysicalSymbol getSymbol() {
        return this;
    }

    @Override
    default @NotNull PsiElement getDeclaringElement() {
        return this;
    }

    @Override
    default @NotNull TextRange getRangeInDeclaringElement() {
        PsiElement element = getNameIdentifier();
        // The text range of the entire element shouldn't be returned
        // Potentially, the symbol should be seperated from the element, and shouldn't be provided if it isn't declared.
        return element != null ? element.getTextRange() : getTextRange();
    }

    @Override
    default @NotNull Pointer<? extends PhysicalSymbol> createPointer() {
        return new PhysicalPointer(this);
    }
}
