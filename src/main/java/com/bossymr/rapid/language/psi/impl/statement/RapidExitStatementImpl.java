package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExitStatement;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;

public class RapidExitStatementImpl extends RapidCompositeElement implements RapidExitStatement {

    public RapidExitStatementImpl() {
        super(RapidElementTypes.EXIT_STATEMENT);
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
