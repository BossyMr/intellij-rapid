package com.bossymr.rapid.ide.editor.insight.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public abstract class RapidQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement implements LocalQuickFix {

    protected RapidQuickFix(@Nullable PsiElement element) {
        super(element);
    }

}
