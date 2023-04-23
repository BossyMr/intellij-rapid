package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.PhysicalElement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExitStatement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class RapidExitStatementImpl extends PhysicalElement implements RapidExitStatement {

    public RapidExitStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitExitStatement(this);
    }

    @Override
    public String toString() {
        return "RapidExitStatement";
    }
}
