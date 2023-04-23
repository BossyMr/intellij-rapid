package com.bossymr.rapid.language.psi.impl.statement;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidIfStatementImpl extends PhysicalElement implements RapidIfStatement {

    public RapidIfStatementImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        RapidStatementList thenBranch = getThenBranch();
        if (thenBranch != null && child == thenBranch.getNode()) {
            replaceChildInternal(SourceTreeToPsiMap.treeElementToPsi(child), ASTFactory.composite(RapidElementTypes.STATEMENT_LIST));
        }
        RapidStatementList elseBranch = getElseBranch();
        if (elseBranch != null && child == elseBranch.getNode()) {
            ASTNode keyword = findChildByType(RapidTokenTypes.ELSE_KEYWORD);
            if (keyword != null) {
                super.deleteChildInternal(keyword);
            }
        }
        super.deleteChildInternal(child);
    }

    @Override
    public @Nullable RapidExpression getCondition() {
        return findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @Nullable RapidStatementList getThenBranch() {
        return findChildByType(RapidElementTypes.STATEMENT_LIST);
    }

    @Override
    public @Nullable RapidStatementList getElseBranch() {
        PsiElement keyword = findChildByType(RapidTokenTypes.ELSE_KEYWORD);
        if (keyword != null) {
            return PsiTreeUtil.getNextSiblingOfType(keyword, RapidStatementList.class);
        }
        return null;
    }

    @Override
    public @NotNull List<RapidIfStatement> getStatements() {
        return PsiTreeUtil.getChildrenOfTypeAsList(this, RapidIfStatement.class);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitIfStatement(this);
    }

    @Override
    public String toString() {
        return "RapidIfStatement";
    }
}
