package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReturnStatement;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidReturnStatementImpl extends RapidCompositeElement implements RapidReturnStatement {

    public RapidReturnStatementImpl() {
        super(RapidElementTypes.RETURN_STATEMENT);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitReturnStatement(this);
    }

    @Override
    public String toString() {
        return "RapidReturnStatement";
    }
}
