package com.bossymr.rapid.language.flow.data;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.conditon.Expression;
import com.bossymr.rapid.language.flow.conditon.Value;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

class DataFlowExpressionVisitor extends ControlFlowVisitor {

    private final @NotNull Consumer<Value.Variable> consumer;

    public DataFlowExpressionVisitor(@NotNull Consumer<Value.Variable> consumer) {
        this.consumer = consumer;
    }

    public static boolean anyMatch(@NotNull Expression expression, @NotNull Predicate<Value.Variable> predicate) {
        AtomicBoolean atomicBoolean = new AtomicBoolean();
        DataFlowExpressionVisitor visitor = new DataFlowExpressionVisitor(variable -> {
            if (!(atomicBoolean.get())) {
                atomicBoolean.set(predicate.test(variable));
            }
        });
        expression.accept(visitor);
        return atomicBoolean.get();
    }

    public static boolean containsVariable(@NotNull Expression expression) {
        return anyMatch(expression, value -> true);
    }

    public static boolean containsVariable(@NotNull Expression expression, @NotNull Value.Variable variable) {
        return anyMatch(expression, value -> value.equals(variable));
    }

    @Override
    public void visitVariableValue(@NotNull Value.Variable value) {
        consumer.accept(value);
    }

    @Override
    public void visitVariableExpression(@NotNull Expression.Variable expression) {
        expression.value().accept(this);
    }

    @Override
    public void visitAggregateExpression(@NotNull Expression.Aggregate expression) {
        for (Value value : expression.values()) {
            value.accept(this);
        }
    }

    @Override
    public void visitBinaryExpression(@NotNull Expression.Binary expression) {
        expression.left().accept(this);
        expression.right().accept(this);
    }

    @Override
    public void visitUnaryExpression(@NotNull Expression.Unary expression) {
        expression.value().accept(this);
    }
}