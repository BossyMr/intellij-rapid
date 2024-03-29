package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidEmptyExpressionImpl extends RapidExpressionImpl implements RapidExpression {

    public RapidEmptyExpressionImpl(@NotNull ASTNode node) {
        super(node);
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
