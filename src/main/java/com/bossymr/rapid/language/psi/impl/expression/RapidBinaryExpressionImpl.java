package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.RapidBinaryExpression;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.type.RapidPrimitiveType;
import com.bossymr.rapid.language.type.RapidType;
import com.bossymr.rapid.language.symbol.ValueType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.bossymr.rapid.language.psi.RapidTokenTypes.*;

public class RapidBinaryExpressionImpl extends RapidExpressionImpl implements RapidBinaryExpression {

    public RapidBinaryExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull RapidExpression getLeft() {
        return (RapidExpression) getFirstChild();
    }

    @Override
    public @Nullable RapidExpression getRight() {
        PsiElement element = getLastChild();
        return element instanceof RapidExpression expression ? expression : null;
    }

    @Override
    public PsiElement getSign() {
        return findChildByType(RapidTokenTypes.OPERATIONS);
    }

    @Override
    public @Nullable RapidType getType() {
        RapidType left = getLeft().getType();
        RapidType right = getRight() != null ? getRight().getType() : null;
        IElementType sign = getSign().getNode().getElementType();
        if (left == null || right == null) return null;
        if (left.getDimensions() == 0 && right.getDimensions() == 0 && List.of(OR_KEYWORD, XOR_KEYWORD, AND_KEYWORD, LT, LE, GT, GE).contains(sign)) {
            return RapidPrimitiveType.BOOLEAN;
        }
        if (sign == LTGT) {
            return RapidPrimitiveType.BOOLEAN;
        }
        if (sign == EQ) {
            if (left.isAssignable(right) && left.getValueType() == ValueType.VALUE_TYPE) {
                return RapidPrimitiveType.BOOLEAN;
            }
        }
        if (left.isAssignable(RapidPrimitiveType.NUMBER) && right.isAssignable(RapidPrimitiveType.NUMBER))
            return RapidPrimitiveType.NUMBER;
        if (left.isAssignable(RapidPrimitiveType.DOUBLE) && right.isAssignable(RapidPrimitiveType.DOUBLE))
            return RapidPrimitiveType.DOUBLE;
        if (sign == ASTERISK) {
            if ((left.isAssignable(RapidPrimitiveType.NUMBER) && right.isAssignable(RapidPrimitiveType.POSITION)))
                return RapidPrimitiveType.POSITION;
            if ((left.isAssignable(RapidPrimitiveType.POSITION) && right.isAssignable(RapidPrimitiveType.NUMBER)))
                return RapidPrimitiveType.POSITION;
            if (left.isAssignable(RapidPrimitiveType.POSITION) && right.isAssignable(RapidPrimitiveType.POSITION))
                return RapidPrimitiveType.POSITION;
            if (left.isAssignable(RapidPrimitiveType.ORIENTATION) && right.isAssignable(RapidPrimitiveType.ORIENTATION))
                return RapidPrimitiveType.ORIENTATION;
        }
        if (sign == BACKSLASH) {
            if (left.isAssignable(RapidPrimitiveType.POSITION) && right.isAssignable(RapidPrimitiveType.NUMBER))
                return RapidPrimitiveType.POSITION;
        }
        if (sign == PLUS) {
            if (left.isAssignable(RapidPrimitiveType.POSITION) && right.isAssignable(RapidPrimitiveType.POSITION))
                return RapidPrimitiveType.POSITION;
            if (left.isAssignable(RapidPrimitiveType.STRING) && right.isAssignable(RapidPrimitiveType.STRING))
                return RapidPrimitiveType.STRING;
        }
        if (sign == MINUS) {
            if (left.isAssignable(RapidPrimitiveType.POSITION) && right.isAssignable(RapidPrimitiveType.POSITION))
                return RapidPrimitiveType.POSITION;
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
