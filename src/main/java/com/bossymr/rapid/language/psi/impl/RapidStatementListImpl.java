package com.bossymr.rapid.language.psi.impl;

import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RapidStatementListImpl extends PhysicalElement implements RapidStatementList {

    public RapidStatementListImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public void accept(@NotNull RapidElementVisitor visitor) {
        visitor.visitStatementList(this);
    }

    @Override
    public BlockType getStatementListType() {
        if (findChildByType(RapidTokenTypes.BACKWARD_KEYWORD) != null) {
            return BlockType.BACKWARD_CLAUSE;
        } else if (findChildByType(RapidTokenTypes.ERROR_KEYWORD) != null) {
            return BlockType.ERROR_CLAUSE;
        } else if (findChildByType(RapidTokenTypes.UNDO_KEYWORD) != null) {
            return BlockType.UNDO_CLAUSE;
        } else {
            return BlockType.STATEMENT_LIST;
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

    public List<PhysicalField> getFields() {
        return findChildrenByType(RapidElementTypes.FIELD);
    }

    @Override
    public String toString() {
        return "RapidStatementList";
    }
}
