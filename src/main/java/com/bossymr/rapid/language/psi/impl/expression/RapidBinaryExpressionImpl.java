package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

public class RapidBinaryExpressionImpl extends RapidExpressionElement implements RapidBinaryExpression {

    public RapidBinaryExpressionImpl() {
        super(RapidElementTypes.BINARY_EXPRESSION);
    }

    @Override
    public @NotNull RapidExpression getLeft() {
        return (RapidExpression) getFirstChildNode();
    }

    @Override
    public @Nullable RapidExpression getRight() {
        return RapidElementTypes.EXPRESSIONS.contains(getLastChildNode().getElementType()) ? (RapidExpression) getLastChildNode() : null;
    }

    @Override
    public PsiElement getSign() {
        return findPsiChildByType(RapidTokenTypes.OPERATIONS);
    }

    @Override
    public @Nullable RapidType getType() {
        RapidType left = getLeft().getType();
        RapidType right = getRight() != null ? getRight().getType() : null;
        IElementType sign = getSign().getNode().getElementType();
        if (left == null || right == null) return null;
        if (List.of(OR_KEYWORD, XOR_KEYWORD, AND_KEYWORD, LT, LE, EQ, GT, GE, LTGT).contains(sign)) {
            return RapidType.BOOLEAN;
        }
        if (left.isAssignable(RapidType.NUMBER) && right.isAssignable(RapidType.NUMBER))
            return RapidType.NUMBER;
        if (left.isAssignable(RapidType.DOUBLE) && right.isAssignable(RapidType.DOUBLE))
            return RapidType.DOUBLE;
        if (sign == ASTERISK) {
            if ((left.isAssignable(RapidType.NUMBER) && right.isAssignable(RapidType.POSITION)))
                return RapidType.POSITION;
            if ((left.isAssignable(RapidType.POSITION) && right.isAssignable(RapidType.NUMBER)))
                return RapidType.POSITION;
            if (left.isAssignable(RapidType.POSITION) && right.isAssignable(RapidType.POSITION))
                return RapidType.POSITION;
            if (left.isAssignable(RapidType.ORIENTATION) && right.isAssignable(RapidType.ORIENTATION))
                return RapidType.ORIENTATION;
        }
        if (sign == BACKSLASH) {
            if (left.isAssignable(RapidType.POSITION) && right.isAssignable(RapidType.NUMBER))
                return RapidType.POSITION;
        }
        if (sign == PLUS) {
            if (left.isAssignable(RapidType.POSITION) && right.isAssignable(RapidType.POSITION))
                return RapidType.POSITION;
            if (left.isAssignable(RapidType.STRING) && right.isAssignable(RapidType.STRING))
                return RapidType.STRING;
        }
        if (sign == MINUS) {
            if (left.isAssignable(RapidType.POSITION) && right.isAssignable(RapidType.POSITION))
                return RapidType.POSITION;
        }
        return null;
    }

    @Override
    public boolean isConstant() {
        return getLeft().isConstant() && (getRight() == null || getRight().isLiteral());
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitBinaryExpression(this);
    }

    @Override
    public String toString() {
        return "RapidBinaryExpression:" + getText();
    }

}
