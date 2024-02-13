package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidParenthesisedExpression;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidParenthesisedExpressionImpl extends RapidExpressionImpl implements RapidParenthesisedExpression {

    public RapidParenthesisedExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidType getType() {
        return getExpression() != null ? getExpression().getType() : null;
    }

    @Override
    public boolean isConstant() {
        return getExpression() == null || getExpression().isConstant();
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
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
