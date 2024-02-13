package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.intellij.codeInsight.intention.FileModifier;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MakeParameterListOptionalFix implements IntentionAction {

    private final PhysicalParameterGroup parameterGroup;

    public MakeParameterListOptionalFix(@NotNull PhysicalParameterGroup parameterGroup) {
        this.parameterGroup = parameterGroup;
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.make.optional");
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.make.optional");
    }

    @Override
    public @NotNull FileModifier getFileModifierForPreview(@NotNull PsiFile target) {
        return new MakeParameterListOptionalFix(PsiTreeUtil.findSameElementInCopy(parameterGroup, target));
    }

    @Override
    public boolean isAvailable(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) {
        return parameterGroup.isValid() && !(parameterGroup.isOptional()) && BaseIntentionAction.canModify(parameterGroup);
    }

    @Override
    public void invoke(@NotNull Project project, @Nullable Editor editor, @Nullable PsiFile file) throws IncorrectOperationException {
        ASTNode node = parameterGroup.getNode();
        LeafElement element = Factory.createSingleLeafElement(RapidTokenTypes.BACKSLASH, "\\", null, PsiManager.getInstance(project));
        node.addChild(element, node.getFirstChildNode());
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
