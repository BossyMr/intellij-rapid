package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidStatement;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.safeDelete.SafeDeleteHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SafeDeleteFix extends LocalQuickFixAndIntentionActionOnPsiElement {

    public SafeDeleteFix(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    public @NotNull String getText() {
        PsiElement startElement = getStartElement();
        String elementName = startElement instanceof RapidSymbol symbol ? symbol.getPresentableName() : RapidSymbol.getDefaultText();
        return RapidBundle.message("quick.fix.name.safe.delete.symbol", elementName);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.safe.delete.symbol");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        if(!(FileModificationService.getInstance().prepareFileForWrite(file))) {
            return;
        }
        SafeDeleteHandler.invoke(project, new PsiElement[]{startElement}, true);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
