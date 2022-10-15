package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidAssignmentStatement;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidAssignmentStatementImpl extends RapidCompositeElement implements RapidAssignmentStatement {

    public RapidAssignmentStatementImpl() {
        super(RapidElementTypes.ASSIGNMENT_STATEMENT);
    }

    @Override
    public @Nullable RapidExpression getLeft() {
        return RapidElementTypes.EXPRESSIONS.contains(getFirstChildNode().getElementType()) ? (RapidExpression) getFirstChildNode() : null;
    }

    @Override
    public @Nullable RapidExpression getRight() {
        return RapidElementTypes.EXPRESSIONS.contains(getLastChildNode().getElementType()) ? (RapidExpression) getLastChildNode() : null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitAssignmentStatement(this);
    }

    @Override
    public String toString() {
        return "RapidAssignmentStatement:" + getText();
    }
}
