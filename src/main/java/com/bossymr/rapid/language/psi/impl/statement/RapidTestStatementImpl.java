package com.bossymr.rapid.language.psi.impl.statement;

import com.intellij.psi.util.PsiTreeUtil;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidTestStatementImpl extends RapidCompositeElement implements RapidTestStatement {

    public RapidTestStatementImpl() {
        super(RapidElementTypes.TEST_STATEMENT);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @NotNull List<RapidTestCaseStatement> getTestCaseStatements() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, RapidTestCaseStatement.class);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTestStatement(this);
    }

    @Override
    public String toString() {
        return "RapidTestStatement";
    }
}
