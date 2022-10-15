package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidGotoStatement;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidGotoStatementImpl extends RapidCompositeElement implements RapidGotoStatement {

    public RapidGotoStatementImpl() {
        super(RapidElementTypes.GOTO_STATEMENT);
    }

    @Override
    public @Nullable RapidReferenceExpression getReferenceExpression() {
        return (RapidReferenceExpression) findChildByType(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitGotoStatement(this);
    }

    @Override
    public String toString() {
        return "RapidGotoStatement";
    }}
