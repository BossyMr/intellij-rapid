package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidFunctionCallExpressionImpl extends RapidExpressionElement implements RapidFunctionCallExpression {

    public RapidFunctionCallExpressionImpl() {
        super(RapidElementTypes.FUNCTION_CALL_EXPRESSION);
    }

    @Override
    public @Nullable RapidType getType() {
        PsiElement element = getReferenceExpression().resolve();
        return element instanceof RapidRoutine ? ((RapidRoutine) element).getType() : null;
    }

    @Override
    public @NotNull RapidArgumentList getArgumentList() {
        return (RapidArgumentList) Objects.requireNonNull(findChildByType(RapidElementTypes.ARGUMENT_LIST));
    }

    @Override
    public @NotNull RapidReferenceExpression getReferenceExpression() {
        return (RapidReferenceExpression) Objects.requireNonNull(findChildByType(RapidElementTypes.REFERENCE_EXPRESSION));
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitFunctionCallExpression(this);
    }
}
