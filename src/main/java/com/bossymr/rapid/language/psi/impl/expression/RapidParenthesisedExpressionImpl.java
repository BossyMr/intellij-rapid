package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidParenthesisedExpressionImpl extends RapidExpressionElement implements RapidParenthesisedExpression {

    public RapidParenthesisedExpressionImpl() {
        super(RapidElementTypes.PARENTHESISED_EXPRESSION);
    }

    @Override
    public @Nullable RapidType getType() {
        return getExpression() != null ? getExpression().getType() : null;
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitParenthesisedExpression(this);
    }

    @Override
    public String toString() {
        return "RapidParenthesisedExpression:" + getText();
    }
}
