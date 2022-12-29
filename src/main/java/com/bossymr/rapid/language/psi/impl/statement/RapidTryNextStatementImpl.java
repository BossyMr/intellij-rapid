package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTryNextStatement;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;

public class RapidTryNextStatementImpl extends RapidElementImpl implements RapidTryNextStatement {

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
