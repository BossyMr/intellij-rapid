package com.bossymr.rapid.ide.editor.inlay;

import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.codeInsight.hints.VcsCodeVisionLanguageContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseEvent;

@SuppressWarnings("UnstableApiUsage")
public class RapidVcsCodeVisionContext implements VcsCodeVisionLanguageContext {
    @Override
    public boolean isAccepted(@NotNull PsiElement element) {
        return element instanceof PhysicalRoutine;
    }

    @Override
    public void handleClick(@NotNull MouseEvent mouseEvent, @NotNull Editor editor, @NotNull PsiElement element) {}
}
