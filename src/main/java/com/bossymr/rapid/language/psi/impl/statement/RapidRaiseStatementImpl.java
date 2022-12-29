package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidRaiseStatement;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidRaiseStatementImpl extends RapidElementImpl implements RapidRaiseStatement {

    public RapidRaiseStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitRaiseStatement(this);
    }

    @Override
    public String toString() {
        return "RapidRaiseStatement";
    }
}
