package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReturnStatement;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RemoveElementFix extends PsiUpdateModCommandAction<PsiElement> {

    private final @NotNull String message;

    public RemoveElementFix(@NotNull PsiElement element, @NotNull String message) {
        super(element);
        this.message = message;
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return message;
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PsiElement element, @NotNull ModPsiUpdater updater) {
        element.delete();
    }
}
