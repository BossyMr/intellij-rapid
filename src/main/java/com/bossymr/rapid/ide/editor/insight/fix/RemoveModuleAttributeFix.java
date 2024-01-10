package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.impl.RapidAttributeListImpl;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RemoveModuleAttributeFix extends RapidQuickFix {

    public RemoveModuleAttributeFix(@NotNull PsiElement attribute) {
        super(attribute);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        PsiElement element = getStartElement();
        return RapidBundle.message("quick.fix.text.remove.module.attribute", element != null ? element.getText() : "");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.remove.module.attribute");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        RapidAttributeListImpl attributeList = ((RapidAttributeListImpl) startElement.getParent());
        attributeList.deleteChildInternal(startElement.getNode());
    }
}
