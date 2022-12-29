package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidElementImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidForStatementImpl extends RapidElementImpl implements RapidForStatement {

    public RapidForStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable RapidTargetVariable getVariable() {
        return findChildByType(RapidElementTypes.TARGET_VARIABLE);
    }

    @Override
    public @Nullable RapidExpression getFromExpression() {
        return getNextExpression(RapidTokenTypes.FROM_KEYWORD);
    }

    @Override
    public @Nullable RapidExpression getToExpression() {
        return getNextExpression(RapidTokenTypes.TO_KEYWORD);
    }

    @Override
    public @Nullable RapidExpression getStepExpression() {
        return getNextExpression(RapidTokenTypes.STEP_KEYWORD);
    }

    private @Nullable RapidExpression getNextExpression(@NotNull IElementType elementType) {
        PsiElement keyword = findChildByType(elementType);
        if (keyword != null) {
            return PsiTreeUtil.getNextSiblingOfType(keyword, RapidExpression.class);
        }
        return null;
    }

    @Override
    public @Nullable RapidStatementList getStatementList() {
        return findChildByType(RapidElementTypes.STATEMENT_LIST);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitForStatement(this);
    }

    @Override
    public String toString() {
        return "RapidForStatement";
    }
}
