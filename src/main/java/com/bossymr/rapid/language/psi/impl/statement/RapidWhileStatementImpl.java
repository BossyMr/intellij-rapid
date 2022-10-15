package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidWhileStatementImpl extends RapidCompositeElement implements RapidWhileStatement {

    public RapidWhileStatementImpl() {
        super(RapidElementTypes.WHILE_STATEMENT);
    }

    @Override
    public @Nullable RapidExpression getCondition() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @Nullable RapidStatementList getStatementList() {
        return (RapidStatementList) findChildByType(RapidElementTypes.STATEMENT_LIST);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitWhileStatement(this);
    }

    @Override
    public String toString() {
        return "RapidWhileStatement";
    }
}
