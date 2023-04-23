package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTryNextStatement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class RapidTryNextStatementImpl extends PhysicalElement implements RapidTryNextStatement {

    public RapidTryNextStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitTryNextStatement(this);
    }

    @Override
    public String toString() {
        return "RapidTryNextStatement";
    }
}
