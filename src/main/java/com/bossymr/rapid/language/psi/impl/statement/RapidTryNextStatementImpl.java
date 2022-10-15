package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidTryNextStatement;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;

public class RapidTryNextStatementImpl extends RapidCompositeElement implements RapidTryNextStatement {

    public RapidTryNextStatementImpl() {
        super(RapidElementTypes.TRY_NEXT_STATEMENT);
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
