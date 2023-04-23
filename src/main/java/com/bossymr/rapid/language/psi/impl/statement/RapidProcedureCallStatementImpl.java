package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class RapidProcedureCallStatementImpl extends PhysicalElement implements RapidProcedureCallStatement {

    public RapidProcedureCallStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isLate() {
        return getFirstChild().getNode().getElementType().equals(RapidTokenTypes.PERCENT);
    }

    @Override
    public @NotNull RapidExpression getReferenceExpression() {
        return findNotNullChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @NotNull RapidArgumentList getArgumentList() {
        return findNotNullChildByType(RapidElementTypes.ARGUMENT_LIST);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitProcedureCallStatement(this);
    }

    @Override
    public String toString() {
        return "RapidProcedureCallStatement:" + getText();
    }
}
