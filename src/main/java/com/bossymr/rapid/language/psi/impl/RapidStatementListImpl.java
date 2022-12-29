package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidStatementListImpl extends RapidElementImpl implements RapidStatementList {

    public RapidStatementListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitStatementList(this);
    }

    @Override
    public Attribute getAttribute() {
        if (findChildByType(RapidTokenTypes.BACKWARD_KEYWORD) != null) {
            return Attribute.BACKWARD_CLAUSE;
        } else if (findChildByType(RapidTokenTypes.ERROR_KEYWORD) != null) {
            return Attribute.ERROR_CLAUSE;
        } else if (findChildByType(RapidTokenTypes.UNDO_KEYWORD) != null) {
            return Attribute.UNDO_CLAUSE;
        } else {
            return Attribute.STATEMENT_LIST;
        }
    }

    @Override
    public @Nullable List<RapidExpression> getExpressions() {
        RapidExpressionList expressionList = findChildByType(RapidElementTypes.EXPRESSION_LIST);
        return expressionList != null ? expressionList.getExpressions() : null;
    }

    @Override
    public List<RapidStatement> getStatements() {
        return findChildrenByType(RapidElementTypes.STATEMENTS);
    }

    @Override
    public String toString() {
        return "RapidStatementList";
    }
}
