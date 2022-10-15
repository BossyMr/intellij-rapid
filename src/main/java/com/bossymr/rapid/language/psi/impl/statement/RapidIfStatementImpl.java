package com.bossymr.rapid.language.psi.impl.statement;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidIfStatementImpl extends RapidCompositeElement implements RapidIfStatement {

    public RapidIfStatementImpl() {
        super(RapidElementTypes.IF_STATEMENT);
    }

    @Override
    public void deleteChildInternal(@NotNull ASTNode child) {
        if(child == getThenBranch()) {
            replaceChildInternal(child, ASTFactory.composite(RapidElementTypes.STATEMENT_LIST));
        }
        if(child == getElseBranch()) {
            ASTNode keyword = findChildByType(RapidTokenTypes.ELSE_KEYWORD);
            if(keyword != null) {
                super.deleteChildInternal(keyword);
            }
        }
        super.deleteChildInternal(child);
    }

    @Override
    public @Nullable RapidExpression getCondition() {
        return (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS);
    }

    @Override
    public @Nullable RapidStatementList getThenBranch() {
        return (RapidStatementList) findChildByType(RapidElementTypes.STATEMENT_LIST, getFirstChildNode());
    }

    @Override
    public @Nullable RapidStatementList getElseBranch() {
        ASTNode keyword = findChildByType(RapidTokenTypes.ELSE_KEYWORD);
        return keyword != null ? (RapidStatementList) findChildByType(RapidElementTypes.STATEMENT_LIST, keyword) : null;
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
