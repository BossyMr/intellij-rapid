package com.bossymr.rapid.language.symbol;

import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.ide.IconProvider;
import com.intellij.platform.backend.presentation.TargetPresentation;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

@SuppressWarnings("UnstableApiUsage")
public class RapidIconProvider extends IconProvider {
    @Override
    public @Nullable Icon getIcon(@NotNull PsiElement element, int flags) {
        if (element instanceof PhysicalSymbol symbol) {
            TargetPresentation presentation = symbol.getTargetPresentation();
            return presentation.getIcon();
        }
        return null;
    }
}
