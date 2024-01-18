package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.ide.editor.insight.unwrap.RapidIfUnwrapper;
import com.bossymr.rapid.language.psi.RapidIfStatement;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class UnwrapIfStatementFix extends PsiUpdateModCommandAction<RapidIfStatement> {

    public UnwrapIfStatementFix(@NotNull RapidIfStatement element) {
        super(element);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.unwrap.if.statement");
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull RapidIfStatement element, @NotNull ModPsiUpdater updater) {
        new RapidIfUnwrapper().doUnwrap(element);
    }
}
