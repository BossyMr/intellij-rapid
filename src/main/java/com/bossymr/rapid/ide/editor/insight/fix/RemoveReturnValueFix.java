package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReturnStatement;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.Presentation;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class RemoveReturnValueFix extends PsiUpdateModCommandAction<RapidReturnStatement> {

    public RemoveReturnValueFix(@NotNull RapidReturnStatement element) {
        super(element);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.remove.return.value");
    }

    @Override
    protected @Nullable Presentation getPresentation(@NotNull ActionContext context, @NotNull RapidReturnStatement element) {
        RapidExpression returnValue = element.getExpression();
        if (returnValue == null) {
            return null;
        }
        return super.getPresentation(context, element);
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull RapidReturnStatement element, @NotNull ModPsiUpdater updater) {
        RapidExpression returnValue = element.getExpression();
        Objects.requireNonNull(returnValue);
        returnValue.delete();
    }
}
