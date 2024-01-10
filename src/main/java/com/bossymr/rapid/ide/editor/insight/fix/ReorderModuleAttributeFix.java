package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReorderModuleAttributeFix extends RapidQuickFix {


    public ReorderModuleAttributeFix(@NotNull RapidAttributeList attributeList) {
        super(attributeList);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.reorder.module.attributes");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.reorder.module.attributes");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        List<ModuleType> attributes = ((RapidAttributeList) startElement).getAttributes();
        RapidElementFactory factory = RapidElementFactory.getInstance(project);
        startElement.replace(factory.createAttributeList(attributes));
    }
}
