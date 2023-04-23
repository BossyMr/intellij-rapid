package com.bossymr.rapid.language.symbol.physical;

import com.bossymr.rapid.ide.documentation.PhysicalDocumentationTarget;
import com.bossymr.rapid.language.psi.RapidElement;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.intellij.model.Symbol;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.refactoring.rename.api.RenameTarget;
import com.intellij.refactoring.rename.symbol.RenameableSymbol;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A {@code PhysicalSymbol} is a symbol which is represented by an element in a source code file.
 */
@SuppressWarnings("UnstableApiUsage")
public interface PhysicalSymbol extends RapidElement, RapidSymbol, PsiNameIdentifierOwner, RenameableSymbol, PsiSymbolDeclaration, NavigatablePsiElement {

    @Override
    default @Nullable String getCanonicalName() {
        return getName();
    }

    @Override
    @NotNull
    default Collection<? extends @NotNull PsiSymbolDeclaration> getOwnDeclarations() {
        if (getNameIdentifier() != null) {
            return List.of(this);
        } else {
            return List.of();
        }
    }

    @Override
    default @NotNull PsiElement getDeclaringElement() {
        return Objects.requireNonNull(getNameIdentifier());
    }

    @Override
    default @NotNull TextRange getRangeInDeclaringElement() {
        return TextRange.from(0, getDeclaringElement().getTextLength());
    }

    @Override
    default @NotNull Symbol getSymbol() {
        return this;
    }

    @Override
    default @NotNull Collection<PhysicalNavigationTarget> getNavigationTargets(@NotNull Project project) {
        return List.of(new PhysicalNavigationTarget(this));
    }

    @Override
    @NotNull PhysicalPointer<? extends PhysicalSymbol> createPointer();

    @Override
    default @NotNull RenameTarget getRenameTarget() {
        return new PhysicalRenameTarget(this);
    }

    @Override
    @NotNull
    default DocumentationTarget getDocumentationTarget() {
        return new PhysicalDocumentationTarget(this);
    }
}
