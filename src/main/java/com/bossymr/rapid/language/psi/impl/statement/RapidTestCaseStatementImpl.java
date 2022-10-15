package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidTestCaseStatementImpl extends RapidCompositeElement implements RapidTestCaseStatement {

    public RapidTestCaseStatementImpl() {
        super(RapidElementTypes.TEST_CASE_STATEMENT);
    }

    @Override
    public boolean isDefault() {
        return getFirstChildNode().getElementType().equals(RapidTokenTypes.DEFAULT_KEYWORD);
    }

    @Override
    public @Nullable RapidExpressionList getExpressionList() {
        return (RapidExpressionList) findChildByType(RapidElementTypes.EXPRESSION_LIST);
    }

    @Override
    public @NotNull RapidStatementList getStatements() {
        return (RapidStatementList) Objects.requireNonNull(findChildByType(RapidElementTypes.STATEMENT_LIST));
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTestCaseStatement(this);
    }

    @Override
    public String toString() {
        return "RapidTestCaseStatement:" + getText();
    }
}
