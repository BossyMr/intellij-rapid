package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidConnectStatement;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidConnectStatementImpl extends RapidElementImpl implements RapidConnectStatement {

    public RapidConnectStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getLeft() {
        List<RapidExpression> expressions = findChildrenByType(RapidElementTypes.EXPRESSIONS);
        return expressions.size() > 0 ? expressions.get(0) : null;
    }

    @Override
    public @Nullable RapidExpression getRight() {
        List<RapidExpression> expressions = findChildrenByType(RapidElementTypes.EXPRESSIONS);
        return expressions.size() > 1 ? expressions.get(1) : null;
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitConnectStatement(this);
    }

    @Override
    public String toString() {
        return "RapidConnectStatement";
    }
}
