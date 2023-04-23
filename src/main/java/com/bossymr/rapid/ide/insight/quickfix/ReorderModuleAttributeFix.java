package com.bossymr.rapid.ide.insight.quickfix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidAttributeList;
import com.bossymr.rapid.language.psi.RapidElementFactory;
import com.bossymr.rapid.language.symbol.ModuleType;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public final class ReorderModuleAttributeFix implements IntentionAction {

    private final RapidAttributeList attributeList;

    public ReorderModuleAttributeFix(@NotNull RapidAttributeList attributeList) {
        this.attributeList = attributeList;
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
    public @NotNull FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return new ReorderModuleAttributeFix(PsiTreeUtil.findSameElementInCopy(attributeList, target));
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        RapidAttributeList copy = RapidElementFactory.getInstance(project).createAttributeList(getAttributes());
        attributeList.replace(copy);
    }

    private @NotNull List<ModuleType> getAttributes() {
        return attributeList.getAttributes().stream().sorted(Comparator.comparing(Enum::ordinal)).distinct().toList();
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        return attributeList.isValid() && BaseIntentionAction.canModify(attributeList);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
