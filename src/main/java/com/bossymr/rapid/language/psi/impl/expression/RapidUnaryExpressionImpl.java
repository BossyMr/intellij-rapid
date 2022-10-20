package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidUnaryExpressionImpl extends RapidExpressionElement implements RapidUnaryExpression {

    public RapidUnaryExpressionImpl() {
        super(RapidElementTypes.UNARY_EXPRESSION);
    }

    @Override
    public @Nullable RapidType getType() {
        RapidExpression expression = getExpression();
        if (expression != null) {
            RapidType type = expression.getType();
            IElementType sign = getSign().getNode().getElementType();
            if (TokenSet.create(RapidTokenTypes.PLUS, RapidTokenTypes.MINUS).contains(sign)) {
                if (type == null) return null;
                return RapidType.NUMBER.apply(getProject()).isAssignable(type) || RapidType.DOUBLE.apply(getProject()).isAssignable(type) ? type : null;
            } else if (RapidTokenTypes.NOT_KEYWORD.equals(sign)) {
                return RapidType.BOOLEAN.apply(getProject());
            }
            throw new RuntimeException();
        } else {
            return null;
        }
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @NotNull PsiElement getSign() {
        return Objects.requireNonNull(findPsiChildByType(RapidTokenTypes.OPERATIONS));
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitUnaryExpression(this);
    }
}
