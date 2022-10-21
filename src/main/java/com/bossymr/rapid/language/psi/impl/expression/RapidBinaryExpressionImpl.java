package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.robot.RobotService.DataType;
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
            return DataType.BOOLEAN.getType(getProject());
        }
        if (left.isAssignable(DataType.NUMBER.getType(getProject())) && right.isAssignable(DataType.NUMBER.getType(getProject())))
            return DataType.NUMBER.getType(getProject());
        if (left.isAssignable(DataType.DOUBLE.getType(getProject())) && right.isAssignable(DataType.DOUBLE.getType(getProject())))
            return DataType.DOUBLE.getType(getProject());
        if (sign == ASTERISK) {
            if ((left.isAssignable(DataType.NUMBER.getType(getProject())) && right.isAssignable(DataType.POSITION.getType(getProject()))))
                return DataType.POSITION.getType(getProject());
            if ((left.isAssignable(DataType.POSITION.getType(getProject())) && right.isAssignable(DataType.NUMBER.getType(getProject()))))
                return DataType.POSITION.getType(getProject());
            if (left.isAssignable(DataType.POSITION.getType(getProject())) && right.isAssignable(DataType.POSITION.getType(getProject())))
                return DataType.POSITION.getType(getProject());
            if (left.isAssignable(DataType.ORIENTATION.getType(getProject())) && right.isAssignable(DataType.ORIENTATION.getType(getProject())))
                return DataType.ORIENTATION.getType(getProject());
        }
        if (sign == BACKSLASH) {
            if (left.isAssignable(DataType.POSITION.getType(getProject())) && right.isAssignable(DataType.NUMBER.getType(getProject())))
                return DataType.POSITION.getType(getProject());
        }
        if (sign == PLUS) {
            if (left.isAssignable(DataType.POSITION.getType(getProject())) && right.isAssignable(DataType.POSITION.getType(getProject())))
                return DataType.POSITION.getType(getProject());
            if (left.isAssignable(DataType.STRING.getType(getProject())) && right.isAssignable(DataType.STRING.getType(getProject())))
                return DataType.STRING.getType(getProject());
        }
        if (sign == MINUS) {
            if (left.isAssignable(DataType.POSITION.getType(getProject())) && right.isAssignable(DataType.POSITION.getType(getProject())))
                return DataType.POSITION.getType(getProject());
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
