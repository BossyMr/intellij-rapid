package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidIndexExpressionImpl extends RapidExpressionImpl implements RapidIndexExpression {

    public RapidIndexExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull RapidExpression getExpression() {
        return findNotNullChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @NotNull RapidArray getArray() {
        return findNotNullChildByType(RapidElementTypes.ARRAY);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitIndexExpression(this);
    }

    @Override
    public @Nullable RapidType getType() {
        RapidType type = getExpression().getType();
        if (type == null) return null;
        int dimensions = getArray().getDimensions().size();
        if (type.getDimensions() < dimensions) return null;
        return type.createArrayType(type.getDimensions() - dimensions);
    }

    @Override
    public boolean isConstant() {
        return true;
    }

    @Override
    public String toString() {
        return "RapidIndexExpression:" + getText();
    }
}
