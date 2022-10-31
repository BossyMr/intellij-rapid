package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidExpressionElement;
import com.bossymr.rapid.language.symbol.RapidType;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidAggregateExpressionImpl extends RapidExpressionElement implements RapidAggregateExpression {

    public RapidAggregateExpressionImpl() {
        super(RapidElementTypes.AGGREGATE_EXPRESSION);
    }

    @Override
    public @NotNull List<RapidExpression> getExpressions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, RapidExpression.class);
    }

    @Override
    public @Nullable RapidType getType() {
        if (getTreeParent() instanceof RapidVariable) {
            return ((RapidVariable) getTreeParent()).getType();
        } else if (getTreeParent() instanceof RapidAggregateExpression) {
            return ((RapidAggregateExpression) getTreeParent()).getType();
        } else if (getTreeParent() instanceof RapidAssignmentStatement) {
            RapidExpression left = ((RapidAssignmentStatement) getTreeParent()).getLeft();
            return left != null ? left.getType() : null;
        }
        return null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitAggregateExpression(this);
    }

    @Override
    public String toString() {
        return "RapidAggregateExpression:" + getText();
    }
}
