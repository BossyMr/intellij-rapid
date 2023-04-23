package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidTestCaseStatementImpl extends PhysicalElement implements RapidTestCaseStatement {

    public RapidTestCaseStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isDefault() {
        return getFirstChild().getNode().getElementType().equals(RapidTokenTypes.DEFAULT_KEYWORD);
    }

    @Override
    public @Nullable List<RapidExpression> getExpressions() {
        RapidExpressionList expressionList = findChildByType(RapidElementTypes.EXPRESSION_LIST);
        return expressionList != null ? expressionList.getExpressions() : null;
    }

    @Override
    public @NotNull RapidStatementList getStatements() {
        return findNotNullChildByType(RapidElementTypes.STATEMENT_LIST);
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
