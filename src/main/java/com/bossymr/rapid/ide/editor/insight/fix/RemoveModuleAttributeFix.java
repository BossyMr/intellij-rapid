package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.impl.RapidAttributeListImpl;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class RemoveModuleAttributeFix extends PsiUpdateModCommandAction<PsiElement> {

    public RemoveModuleAttributeFix(@NotNull PsiElement attribute) {
        super(attribute);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.remove.module.attribute");
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PsiElement element) {
        return Presentation.of(RapidBundle.message("quick.fix.text.remove.module.attribute", element.getText()));
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PsiElement element, @NotNull ModPsiUpdater updater) {
        RapidAttributeListImpl attributeList = ((RapidAttributeListImpl) element.getParent());
        attributeList.deleteChildInternal(element.getNode());
    }
}
