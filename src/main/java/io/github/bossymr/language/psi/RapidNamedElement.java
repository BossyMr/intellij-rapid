package io.github.bossymr.language.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a named element, or symbol, which can be declared and referenced.
 */
public interface RapidNamedElement extends PsiNameIdentifierOwner, NavigatablePsiElement {

    /**
     * Returns the name of this symbol.
     *
     * @return the name of this symbol, or {@code null} if the name is not complete.
     */
    @Override
    @Nullable String getName();
}
