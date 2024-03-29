package com.bossymr.rapid.ide.editor.refactoring;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidRefactoringSupportProvider extends RefactoringSupportProvider {


    @Override
    public boolean isMemberInplaceRenameAvailable(@NotNull PsiElement elementToRename, @Nullable PsiElement context) {
        return elementToRename instanceof PhysicalSymbol;
    }

    @Override
    public boolean isSafeDeleteAvailable(@NotNull PsiElement element) {
        return element instanceof PhysicalSymbol;
    }
}
