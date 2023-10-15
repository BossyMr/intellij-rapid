package com.bossymr.rapid.language.flow.parser;

import com.bossymr.rapid.language.builder.RapidCodeBlockBuilder;
import com.bossymr.rapid.language.builder.RapidCodeBuilder;
import com.bossymr.rapid.language.flow.value.Expression;
import com.bossymr.rapid.language.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

class ControlFlowExpressionVisitor extends RapidElementVisitor {

    private final @NotNull RapidCodeBuilder builder;

    private final @NotNull AtomicReference<Expression> result = new AtomicReference<>();

    public ControlFlowExpressionVisitor(@NotNull RapidCodeBuilder builder) {
        this.builder = builder;
    }

    public static @NotNull Expression getExpression(@NotNull RapidExpression expression, @NotNull RapidCodeBuilder builder) {
        ControlFlowExpressionVisitor visitor = new ControlFlowExpressionVisitor(builder);
        expression.accept(visitor);
        return Objects.requireNonNull(visitor.getResult());
    }

    public @NotNull Expression getResult() {
        return Objects.requireNonNull(result.get());
    }

    @Override
    public void visitAggregateExpression(@NotNull RapidAggregateExpression expression) {
        super.visitAggregateExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull RapidUnaryExpression expression) {
        super.visitUnaryExpression(expression);
    }

    @Override
    public void visitReferenceExpression(@NotNull RapidReferenceExpression expression) {
        super.visitReferenceExpression(expression);
    }

    @Override
    public void visitBinaryExpression(@NotNull RapidBinaryExpression expression) {
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitFunctionCallExpression(@NotNull RapidFunctionCallExpression expression) {
        super.visitFunctionCallExpression(expression);
    }

    @Override
    public void visitIndexExpression(@NotNull RapidIndexExpression expression) {
        super.visitIndexExpression(expression);
    }

    @Override
    public void visitParenthesisedExpression(@NotNull RapidParenthesisedExpression expression) {
        super.visitParenthesisedExpression(expression);
    }

    @Override
    public void visitLiteralExpression(@NotNull RapidLiteralExpression expression) {
        super.visitLiteralExpression(expression);
    }
}
