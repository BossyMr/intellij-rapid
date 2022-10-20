package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
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
            return RapidType.BOOLEAN.apply(getProject());
        }
        if (left.isAssignable(RapidType.NUMBER.apply(getProject())) && right.isAssignable(RapidType.NUMBER.apply(getProject())))
            return RapidType.NUMBER.apply(getProject());
        if (left.isAssignable(RapidType.DOUBLE.apply(getProject())) && right.isAssignable(RapidType.DOUBLE.apply(getProject())))
            return RapidType.DOUBLE.apply(getProject());
        if (sign == ASTERISK) {
            if ((left.isAssignable(RapidType.NUMBER.apply(getProject())) && right.isAssignable(RapidType.POSITION.apply(getProject()))))
                return RapidType.POSITION.apply(getProject());
            if ((left.isAssignable(RapidType.POSITION.apply(getProject())) && right.isAssignable(RapidType.NUMBER.apply(getProject()))))
                return RapidType.POSITION.apply(getProject());
            if (left.isAssignable(RapidType.POSITION.apply(getProject())) && right.isAssignable(RapidType.POSITION.apply(getProject())))
                return RapidType.POSITION.apply(getProject());
            if (left.isAssignable(RapidType.ORIENTATION.apply(getProject())) && right.isAssignable(RapidType.ORIENTATION.apply(getProject())))
                return RapidType.ORIENTATION.apply(getProject());
        }
        if (sign == BACKSLASH) {
            if (left.isAssignable(RapidType.POSITION.apply(getProject())) && right.isAssignable(RapidType.NUMBER.apply(getProject())))
                return RapidType.POSITION.apply(getProject());
        }
        if (sign == PLUS) {
            if (left.isAssignable(RapidType.POSITION.apply(getProject())) && right.isAssignable(RapidType.POSITION.apply(getProject())))
                return RapidType.POSITION.apply(getProject());
            if (left.isAssignable(RapidType.STRING.apply(getProject())) && right.isAssignable(RapidType.STRING.apply(getProject())))
                return RapidType.STRING.apply(getProject());
        }
        if (sign == MINUS) {
            if (left.isAssignable(RapidType.POSITION.apply(getProject())) && right.isAssignable(RapidType.POSITION.apply(getProject())))
                return RapidType.POSITION.apply(getProject());
        }
        return null;
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
