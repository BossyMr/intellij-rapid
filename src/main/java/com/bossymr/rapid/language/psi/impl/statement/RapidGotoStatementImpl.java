package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidElementTypes;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidGotoStatement;
import com.bossymr.rapid.language.psi.RapidReferenceExpression;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidGotoStatementImpl extends RapidElementImpl implements RapidGotoStatement {

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
