package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.symbol.RapidRoutine;
import com.bossymr.rapid.language.symbol.RapidSymbol;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidFunctionCallExpressionImpl extends RapidExpressionImpl implements RapidFunctionCallExpression {

    public RapidFunctionCallExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidType getType() {
        RapidSymbol element = getReferenceExpression().getSymbol();
        return element instanceof RapidRoutine ? ((RapidRoutine) element).getType() : null;
    }

    @Override
    public boolean isConstant() {
        return false;
    }

    @Override
    public @NotNull RapidArgumentList getArgumentList() {
        return findNotNullChildByType(RapidElementTypes.ARGUMENT_LIST);
    }

    @Override
    public @NotNull RapidReferenceExpression getReferenceExpression() {
        return findNotNullChildByType(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitFunctionCallExpression(this);
    }

    @Override
    public String toString() {
        return "RapidFunctionCallExpression:" + getText();
    }
}
