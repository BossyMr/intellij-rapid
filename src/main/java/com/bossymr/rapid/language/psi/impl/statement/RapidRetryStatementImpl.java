package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidRetryStatement;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;

public class RapidRetryStatementImpl extends RapidCompositeElement implements RapidRetryStatement {

    public RapidRetryStatementImpl() {
        super(RapidElementTypes.RETRY_STATEMENT);
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
