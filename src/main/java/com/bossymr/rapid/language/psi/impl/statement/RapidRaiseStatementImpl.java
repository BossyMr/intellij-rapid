package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidRaiseStatementImpl extends RapidCompositeElement implements RapidRaiseStatement {

    public RapidRaiseStatementImpl() {
        super(RapidElementTypes.RAISE_STATEMENT);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRaiseStatement(this);
    }

    @Override
    public String toString() {
        return "RapidRaiseStatement";
    }
}
