package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.safeDelete.SafeDeleteHandler;
import org.jetbrains.annotations.NotNull;

public final class SafeDeleteFix implements LocalQuickFix {

    private final String element;

    public SafeDeleteFix(@NotNull PhysicalSymbol symbol) {
        this.element = symbol.getName();
    }

    @Override
    public @IntentionName @NotNull String getName() {
        return RapidBundle.message("quick.fix.name.safe.delete.symbol", element);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.safe.delete.symbol");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        if (descriptor.getPsiElement() instanceof PhysicalSymbol symbol) {
            SafeDeleteHandler.invoke(project, new PsiElement[]{symbol}, true);
        }
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
