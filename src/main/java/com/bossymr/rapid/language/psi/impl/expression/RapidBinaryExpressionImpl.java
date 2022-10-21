package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.robot.RobotService;
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
        RobotService.Type type = RobotService.getInstance(getProject()).getType();
        if (left == null || right == null) return null;
        if (List.of(OR_KEYWORD, XOR_KEYWORD, AND_KEYWORD, LT, LE, EQ, GT, GE, LTGT).contains(sign)) {
            return type.getBool();
        }
        if (left.isAssignable(type.getNumber()) && right.isAssignable(type.getNumber()))
            return type.getNumber();
        if (left.isAssignable(type.getDouble()) && right.isAssignable(type.getDouble()))
            return type.getDouble();
        if (sign == ASTERISK) {
            if ((left.isAssignable(type.getNumber()) && right.isAssignable(type.getPosition())))
                return type.getPosition();
            if ((left.isAssignable(type.getPosition()) && right.isAssignable(type.getNumber())))
                return type.getPosition();
            if (left.isAssignable(type.getPosition()) && right.isAssignable(type.getPosition()))
                return type.getPosition();
            if (left.isAssignable(type.getOrientation()) && right.isAssignable(type.getOrientation()))
                return type.getOrientation();
        }
        if (sign == BACKSLASH) {
            if (left.isAssignable(type.getPosition()) && right.isAssignable(type.getNumber()))
                return type.getPosition();
        }
        if (sign == PLUS) {
            if (left.isAssignable(type.getPosition()) && right.isAssignable(type.getPosition()))
                return type.getPosition();
            if (left.isAssignable(type.getString()) && right.isAssignable(type.getString()))
                return type.getString();
        }
        if (sign == MINUS) {
            if (left.isAssignable(type.getPosition()) && right.isAssignable(type.getPosition()))
                return type.getPosition();
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
