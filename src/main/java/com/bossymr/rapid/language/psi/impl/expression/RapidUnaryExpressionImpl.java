package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidUnaryExpressionImpl extends RapidExpressionImpl implements RapidUnaryExpression {

    public RapidUnaryExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isLiteral() {
        return getExpression() == null || getExpression().isLiteral();
    }

    @Override
    public @Nullable RapidType getType() {
        RapidExpression expression = getExpression();
        if (expression != null) {
            RapidType rapidType = expression.getType();
            IElementType sign = getSign().getNode().getElementType();
            if (rapidType == null) return null;
            if (TokenSet.create(RapidTokenTypes.PLUS, RapidTokenTypes.MINUS).contains(sign)) {
                return RapidPrimitiveType.NUMBER.isAssignable(rapidType) || RapidPrimitiveType.DOUBLE.isAssignable(rapidType) ? rapidType : null;
            } else if (RapidTokenTypes.NOT_KEYWORD.equals(sign)) {
                return RapidPrimitiveType.BOOLEAN.isAssignable(rapidType) ? rapidType : null;
            }
            throw new RuntimeException();
        } else {
            return null;
        }
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
    public @NotNull PsiElement getSign() {
        return findNotNullChildByType(RapidTokenTypes.OPERATIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitUnaryExpression(this);
    }

    @Override
    public String toString() {
        return "RapidUnaryExpression:" + getText();
    }
}
