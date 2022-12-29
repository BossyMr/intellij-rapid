package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidWhileStatementImpl extends RapidElementImpl implements RapidWhileStatement {

    public RapidWhileStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getCondition() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @Nullable RapidStatementList getStatementList() {
        return findChildByType(RapidElementTypes.STATEMENT_LIST);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitWhileStatement(this);
    }

    @Override
    public String toString() {
        return "RapidWhileStatement";
    }
}
