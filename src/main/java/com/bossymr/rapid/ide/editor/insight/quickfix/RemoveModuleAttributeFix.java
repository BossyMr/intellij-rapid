package com.bossymr.rapid.ide.editor.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.impl.RapidAttributeListImpl;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RemoveModuleAttributeFix implements IntentionAction {

    private final PsiElement attribute;

    public RemoveModuleAttributeFix(@NotNull PsiElement attribute) {
        this.attribute = attribute;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.remove.module.attribute", attribute.getText());
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.remove.module.attribute");
    }

    @Override
    public @NotNull FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return new RemoveModuleAttributeFix(PsiTreeUtil.findSameElementInCopy(attribute, target));
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        RapidAttributeListImpl attributeList = ((RapidAttributeListImpl) attribute.getParent());
        attributeList.deleteChildInternal(attribute.getNode());
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        return attribute.isValid() && BaseIntentionAction.canModify(attribute);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
