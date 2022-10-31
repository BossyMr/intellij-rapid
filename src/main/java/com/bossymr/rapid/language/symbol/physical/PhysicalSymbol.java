package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.Pointer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;

public interface PhysicalSymbol extends RapidSymbol, RapidElement, PsiNameIdentifierOwner, NavigatablePsiElement {

    @Override
    default @NotNull Pointer<PhysicalSymbol> createPointer() {
        return new PhysicalPointer(this);
    }

}
