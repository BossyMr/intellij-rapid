package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidGotoStatementImpl extends PhysicalElement implements RapidGotoStatement {

    public RapidGotoStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidReferenceExpression getReferenceExpression() {
        return findChildByType(RapidElementTypes.REFERENCE_EXPRESSION);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitGotoStatement(this);
    }

    @Override
    public String toString() {
        return "RapidGotoStatement";
    }}
