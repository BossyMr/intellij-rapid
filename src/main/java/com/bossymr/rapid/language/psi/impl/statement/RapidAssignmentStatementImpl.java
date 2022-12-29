package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidAssignmentStatement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidAssignmentStatementImpl extends RapidElementImpl implements RapidAssignmentStatement {

    public RapidAssignmentStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getLeft() {
        return getFirstChild() instanceof RapidExpression expression ? expression : null;
    }

    @Override
    public @Nullable RapidExpression getRight() {
        return getLastChild() instanceof RapidExpression expression ? expression : null;
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
