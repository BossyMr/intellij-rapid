package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidTestStatementImpl extends RapidElementImpl implements RapidTestStatement {

    public RapidTestStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @NotNull List<RapidTestCaseStatement> getTestCaseStatements() {
        return findChildrenByType(RapidElementTypes.TEST_CASE_STATEMENT);
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
