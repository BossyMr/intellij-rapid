package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.surrounder.RapidStatementsSurroundDescriptor;
import com.bossymr.rapid.ide.editor.insight.surrounder.RapidWithIfSurrounder;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class SurroundWithIfStatementFix extends PsiUpdateModCommandQuickFix {

    private final @NotNull String text;

    public SurroundWithIfStatementFix(@NotNull String text) {
        this.text = text;
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.surround.with.if.statement");
    }

    @Override
    public @NotNull String getName() {
        return RapidBundle.message("quick.fix.text.surround.with.if.statement", text);
    }

    @Override
    protected void applyFix(@NotNull Project project, @NotNull PsiElement element, @NotNull ModPsiUpdater updater) {
        PsiElement[] elements = RapidStatementsSurroundDescriptor.getStatementsInOffset(element);
        if(elements == null || elements.length == 0) {
            return;
        }
        new RapidWithIfSurrounder().surroundElements(project, elements, text);
    }
}
