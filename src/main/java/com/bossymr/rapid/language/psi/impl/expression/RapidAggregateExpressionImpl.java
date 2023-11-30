package com.bossymr.rapid.language.psi.impl.expression;

import com.bossymr.rapid.language.psi.RapidAggregateExpression;
import com.bossymr.rapid.language.psi.RapidAssignmentStatement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidExpressionImpl;
import com.bossymr.rapid.language.symbol.RapidVariable;
import com.bossymr.rapid.language.type.RapidType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidAggregateExpressionImpl extends RapidExpressionImpl implements RapidAggregateExpression {

    public RapidAggregateExpressionImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public boolean isConstant() {
        return getExpressions().stream().allMatch(RapidExpression::isConstant);
    }

    @Override
    public boolean isLiteral() {
        return getExpressions().stream().allMatch(RapidExpression::isLiteral);
    }

    @Override
    public @NotNull List<RapidExpression> getExpressions() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, RapidExpression.class);
    }

    @Override
    public @Nullable RapidType getType() {
        if (getParent() instanceof RapidVariable) {
            return ((RapidVariable) getParent()).getType();
        } else if (getParent() instanceof RapidAggregateExpression) {
            RapidType parentType = ((RapidAggregateExpression) getParent()).getType();
            if (parentType == null) {
                return null;
            }
            int dimensions = parentType.getDimensions();
            if (dimensions < 1) {
                return null;
            }
            return parentType.createArrayType(dimensions - 1);
        } else if (getParent() instanceof RapidAssignmentStatement) {
            RapidExpression left = ((RapidAssignmentStatement) getParent()).getLeft();
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
