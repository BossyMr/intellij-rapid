package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidReturnStatement;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidReturnStatementImpl extends RapidElementImpl implements RapidReturnStatement {

    public RapidReturnStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getExpression() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitReturnStatement(this);
    }

    @Override
    public String toString() {
        return "RapidReturnStatement";
    }
}
