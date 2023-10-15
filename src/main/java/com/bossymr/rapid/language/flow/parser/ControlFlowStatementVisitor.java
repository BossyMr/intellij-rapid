package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.flow.value.ReferenceExpression;
import com.bossymr.rapid.language.psi.*;
import com.bossymr.rapid.language.symbol.RapidLabelStatement;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import org.jetbrains.annotations.NotNull;

class ControlFlowStatementVisitor extends RapidElementVisitor {

    private final @NotNull RapidCodeBlockBuilder builder;

    public ControlFlowStatementVisitor(@NotNull RapidCodeBlockBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void visitAssignmentStatement(@NotNull RapidAssignmentStatement statement) {
        if (statement.getLeft() == null || statement.getRight() == null) {
            builder.error(statement);
            return;
        }
        Expression left = ControlFlowExpressionVisitor.getExpression(statement.getLeft(), builder);
        if (!(left instanceof ReferenceExpression referenceExpression)) {
            builder.error(statement);
            return;
        }
        Expression right = controlFlowElementBuilder.getExpression(statement.getRight(), builder);
        builder.assign(statement, referenceExpression, right);
    }

    @Override
    public void visitConnectStatement(@NotNull RapidConnectStatement statement) {
        if (statement.getLeft() == null) {
            builder.error(statement);
            return;
        }
        if (!(statement.getRight() instanceof RapidReferenceExpression trapReference)) {
            builder.error(statement);
            return;
        }
        if (!(trapReference instanceof PhysicalRoutine routine) || routine.getRoutineType() != RoutineType.TRAP || routine.getName() == null) {
            builder.error(statement);
            return;
        }
        Expression left = controlFlowElementBuilder.getExpression(statement.getLeft(), builder);
        if (!(left instanceof ReferenceExpression referenceExpression)) {
            builder.error(statement);
            return;
        }
        Expression right = controlFlowElementBuilder.getExpression(statement.getRight(), builder);
        builder.connect(statement, referenceExpression, right);
    }

    @Override
    public void visitProcedureCallStatement(@NotNull RapidProcedureCallStatement statement) {
        super.visitProcedureCallStatement(statement);
    }

    @Override
    public void visitGotoStatement(@NotNull RapidGotoStatement statement) {
        super.visitGotoStatement(statement);
    }

    @Override
    public void visitReturnStatement(@NotNull RapidReturnStatement statement) {
        super.visitReturnStatement(statement);
    }

    @Override
    public void visitExitStatement(@NotNull RapidExitStatement statement) {
        super.visitExitStatement(statement);
    }

    @Override
    public void visitRaiseStatement(@NotNull RapidRaiseStatement statement) {
        super.visitRaiseStatement(statement);
    }

    @Override
    public void visitRetryStatement(@NotNull RapidRetryStatement statement) {
        super.visitRetryStatement(statement);
    }

    @Override
    public void visitTryNextStatement(@NotNull RapidTryNextStatement statement) {
        super.visitTryNextStatement(statement);
    }

    @Override
    public void visitIfStatement(@NotNull RapidIfStatement statement) {
        super.visitIfStatement(statement);
    }

    @Override
    public void visitForStatement(@NotNull RapidForStatement statement) {
        super.visitForStatement(statement);
    }

    @Override
    public void visitWhileStatement(@NotNull RapidWhileStatement statement) {
        super.visitWhileStatement(statement);
    }

    @Override
    public void visitTestStatement(@NotNull RapidTestStatement statement) {
        super.visitTestStatement(statement);
    }

    @Override
    public void visitLabel(@NotNull RapidLabelStatement statement) {
        super.visitLabel(statement);
    }
}
