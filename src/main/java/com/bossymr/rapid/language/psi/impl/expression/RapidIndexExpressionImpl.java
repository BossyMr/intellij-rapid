package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidIndexExpressionImpl extends RapidExpressionElement implements RapidIndexExpression {

    public RapidIndexExpressionImpl() {
        super(RapidElementTypes.INDEX_EXPRESSION);
    }

    @Override
    public @NotNull RapidExpression getExpression() {
        return (RapidExpression) Objects.requireNonNull(findChildByType(RapidElementTypes.EXPRESSIONS));
    }

    @Override
    public @NotNull RapidArray getArray() {
        return (RapidArray) Objects.requireNonNull(findChildByType(RapidElementTypes.ARRAY));
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitIndexExpression(this);
    }

    @Override
    public @Nullable RapidType getType() {
        RapidType type = getExpression().getType();
        return type != null ? type.createArrayType(0) : null;
    }
}
