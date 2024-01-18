package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.surrounder.RapidWithIfSurrounder;
import com.bossymr.rapid.language.psi.RapidIfStatement;
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
        RapidIfStatement statement = new RapidWithIfSurrounder().surroundElements(project, element.getParent(), new PsiElement[]{element}, text);
        element.getParent().addBefore(statement, element);
        element.delete();
    }
}
