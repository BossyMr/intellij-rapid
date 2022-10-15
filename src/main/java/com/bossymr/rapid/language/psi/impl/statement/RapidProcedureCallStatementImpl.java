package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RapidProcedureCallStatementImpl extends RapidCompositeElement implements RapidProcedureCallStatement {

    public RapidProcedureCallStatementImpl() {
        super(RapidElementTypes.PROCEDURE_CALL_STATEMENT);
    }

    @Override
    public boolean isLate() {
        return getFirstChildNode().getElementType().equals(RapidTokenTypes.PERCENT);
    }

    @Override
    public @NotNull RapidExpression getReferenceExpression() {
        return (RapidExpression) Objects.requireNonNull(findChildByType(RapidElementTypes.EXPRESSIONS));
    }

    @Override
    public @NotNull RapidArgumentList getArgumentList() {
        return (RapidArgumentList) Objects.requireNonNull(findChildByType(RapidElementTypes.ARGUMENT_LIST));
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
