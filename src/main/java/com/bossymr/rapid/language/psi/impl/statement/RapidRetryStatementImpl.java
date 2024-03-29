package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidRetryStatement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class RapidRetryStatementImpl extends PhysicalElement implements RapidRetryStatement {

    public RapidRetryStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRetryStatement(this);
    }

    @Override
    public String toString() {
        return "RapidRetryStatement";
    }
}
