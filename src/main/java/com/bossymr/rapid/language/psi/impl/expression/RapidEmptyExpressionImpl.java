package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidEmptyExpressionImpl extends RapidExpressionElement implements RapidExpression {

    public RapidEmptyExpressionImpl() {
        super(RapidElementTypes.EMPTY_EXPRESSION);
    }

    @Override
    public @Nullable RapidType getType() {
        return null;
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitExpression(this);
    }

    @Override
    public String toString() {
        return "RapidEmptyExpression";
    }
}
