package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class NavigateToAlreadyDeclaredSymbolFix extends RapidQuickFix {


    public NavigateToAlreadyDeclaredSymbolFix(@NotNull PhysicalSymbol symbol) {
        super(symbol);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        PhysicalSymbol element = (PhysicalSymbol) getStartElement();
        String name = element != null ? element.getName() : null;
        return RapidBundle.message("quick.fix.text.navigate.to.already.declared.symbol", name != null ? name : "");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.navigate.to.already.declared.symbol");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        ((PhysicalSymbol) startElement).navigate(true);
    }

    @Override
    public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        PhysicalSymbol element = (PhysicalSymbol) getStartElement();
        if (element == null) {
            return IntentionPreviewInfo.EMPTY;
        }
        return IntentionPreviewInfo.navigate(element);
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }
}
