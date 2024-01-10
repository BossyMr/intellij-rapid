package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.symbol.physical.PhysicalSymbol;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.actions.RenameElementAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class InvokeRenameElementFix extends PsiUpdateModCommandAction<PhysicalSymbol> {

    protected InvokeRenameElementFix(@NotNull PhysicalSymbol element) {
        super(element);
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull PhysicalSymbol element, @NotNull ModPsiUpdater updater) {

    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull PhysicalSymbol element) {
        return super.getPresentation(context, element);
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return RapidBundle.message("quick.fix.family.rename.element");
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return RapidBundle.message("quick.fix.text.rename.symbol");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @Nullable Editor editor, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        RenameElementAction action = new RenameElementAction();
        DataContext context = SimpleDataContext.builder()
                                               .add(CommonDataKeys.PSI_ELEMENT, startElement)
                                               .build();
        ActionUtil.invokeAction(action, context, ActionPlaces.UNKNOWN, null, null);
    }
}
