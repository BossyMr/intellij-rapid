package com.bossymr.rapid.ide.editor.insight.fix;

import com.bossymr.rapid.RapidBundle;
import com.bossymr.rapid.language.psi.RapidTestCaseStatement;
import com.bossymr.rapid.language.psi.RapidTestStatement;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.modcommand.ActionContext;
import com.intellij.modcommand.ModPsiUpdater;
import com.intellij.modcommand.PsiUpdateModCommandAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ReorderTestStatementFix extends PsiUpdateModCommandAction<RapidTestStatement> {

    public ReorderTestStatementFix(@NotNull RapidTestStatement attributeList) {
        super(attributeList);
    }

    @Override
    public @NotNull @IntentionFamilyName String getFamilyName() {
        return RapidBundle.message("quick.fix.family.reorder.module.attributes");
    }

    @Override
    protected void invoke(@NotNull ActionContext context, @NotNull RapidTestStatement element, @NotNull ModPsiUpdater updater) {
        List<RapidTestCaseStatement> statements = new ArrayList<>(element.getTestCaseStatements());
        statements.sort(Comparator.comparing(statement -> statement.isDefault() ? 1 : 0));
        List<RapidTestCaseStatement> caseStatements = element.getTestCaseStatements();
        for (int i = 0; i < caseStatements.size(); i++) {
            RapidTestCaseStatement statement = caseStatements.get(i);
            statement.replace(statements.get(i));
        }
    }
}
