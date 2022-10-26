package com.bossymr.rapid.language.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * Represents an element which is a named symbol.
 */
public interface RapidSymbol extends RapidElement, PsiNameIdentifierOwner, NavigatablePsiElement {

    /**
     * Checks if this symbol is virtual and is represented by a symbol on a connected robot. Virtual symbols are not
     * backed by an element on a file.
     *
     * @return if this symbol is virtual.
     */
    default boolean isVirtual() {
        return false;
    }
}