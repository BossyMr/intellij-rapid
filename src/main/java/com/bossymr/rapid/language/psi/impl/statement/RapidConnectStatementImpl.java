package com.bossymr.rapid.language.psi.impl.statement;

import com.intellij.lang.ASTNode;
import com.bossymr.rapid.language.psi.RapidConnectStatement;
import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidConnectStatementImpl extends RapidCompositeElement implements RapidConnectStatement {

    public RapidConnectStatementImpl() {
        super(RapidElementTypes.CONNECT_STATEMENT);
    }

    @Override
    public @Nullable RapidExpression getLeft() {
        ASTNode[] children = getChildren(RapidElementTypes.EXPRESSIONS);
        return children.length > 0 ? (RapidExpression) children[0] : null;
    }

    @Override
    public @Nullable RapidExpression getRight() {
        ASTNode[] children = getChildren(RapidElementTypes.EXPRESSIONS);
        return children.length > 1 ? (RapidExpression) children[1] : null;
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
