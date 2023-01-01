package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.RapidAssignmentStatement;
import com.bossymr.rapid.language.psi.RapidElementVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.psi.RapidTokenTypes;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidAssignmentStatementImpl extends RapidElementImpl implements RapidAssignmentStatement {

    public RapidAssignmentStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidExpression getLeft() {
        return PsiTreeUtil.getPrevSiblingOfType(getEquals(), RapidExpression.class);
    }

    @Override
    public @Nullable RapidExpression getRight() {
        return PsiTreeUtil.getNextSiblingOfType(getEquals(), RapidExpression.class);
    }

    public @NotNull PsiElement getEquals() {
        return findNotNullChildByType(RapidTokenTypes.CEQ);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitAssignmentStatement(this);
    }

    @Override
    public String toString() {
        return "RapidAssignmentStatement:" + getText();
    }
}
