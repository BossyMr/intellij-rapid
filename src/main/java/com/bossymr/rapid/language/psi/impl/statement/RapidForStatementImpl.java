package com.bossymr.rapid.language.psi.impl.statement;

import com.intellij.lang.ASTNode;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.psi.impl.RapidCompositeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RapidForStatementImpl extends RapidCompositeElement implements RapidForStatement {

    public RapidForStatementImpl() {
        super(RapidElementTypes.FOR_STATEMENT);
    }

    @Override
    public @Nullable RapidTargetVariable getVariable() {
        return (RapidTargetVariable) findChildByType(RapidElementTypes.TARGET_VARIABLE);
    }

    @Override
    public @Nullable RapidExpression getFromExpression() {
        ASTNode keyword = findChildByType(RapidTokenTypes.FROM_KEYWORD);
        return keyword != null ? (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS, keyword) : null;
    }

    @Override
    public @Nullable RapidExpression getToExpression() {
        ASTNode keyword = findChildByType(RapidTokenTypes.TO_KEYWORD);
        return keyword != null ? (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS, keyword) : null;
    }

    @Override
    public @Nullable RapidExpression getStepExpression() {
        ASTNode keyword = findChildByType(RapidTokenTypes.STEP_KEYWORD);
        return keyword != null ? (RapidExpression) findChildByType(RapidElementTypes.EXPRESSIONS, keyword) : null;
    }

    @Override
    public @Nullable RapidStatementList getStatementList() {
        return (RapidStatementList) findChildByType(RapidElementTypes.STATEMENT_LIST);
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
