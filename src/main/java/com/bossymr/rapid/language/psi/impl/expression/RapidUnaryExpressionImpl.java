package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.robot.RobotService;
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
            RapidType rapidType = expression.getType();
            IElementType sign = getSign().getNode().getElementType();
            if (TokenSet.create(RapidTokenTypes.PLUS, RapidTokenTypes.MINUS).contains(sign)) {
                if (rapidType == null) return null;
                return RobotService.DataType.NUMBER.getType(getProject()).isAssignable(rapidType) || RobotService.DataType.DOUBLE.getType(getProject()).isAssignable(rapidType) ? rapidType : null;
            } else if (RapidTokenTypes.NOT_KEYWORD.equals(sign)) {
                return RobotService.DataType.BOOLEAN.getType(getProject());
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
