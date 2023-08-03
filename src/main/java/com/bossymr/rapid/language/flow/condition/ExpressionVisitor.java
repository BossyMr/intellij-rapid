package com.bossymr.rapid.language.flow.condition;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ExpressionVisitor extends ControlFlowVisitor {

    public static @NotNull Expression modify(@NotNull Expression expression, @NotNull Function<ReferenceValue, ReferenceValue> mapper) {
        AtomicReference<Expression> result = new AtomicReference<>(expression);
        expression.accept(new ExpressionVisitor() {
            @Override
            protected @NotNull ReferenceValue process(@NotNull ReferenceValue value) {
                return mapper.apply(value);
            }

            @Override
            protected void update(@NotNull Expression expression) {
                result.set(expression);
            }
        });
        return Objects.requireNonNull(result.get());
    }

    public static @NotNull ExpressionVisitor iterate(@NotNull Function<ReferenceValue, ReferenceValue> mapper, @NotNull Consumer<Expression> consumer) {
        return new ExpressionVisitor() {
            @Override
            protected @NotNull ReferenceValue process(@NotNull ReferenceValue value) {
                return Objects.requireNonNull(mapper.apply(value));
            }

            @Override
            protected void update(@NotNull Expression expression) {
                consumer.accept(expression);
            }
        };
    }

    public static @NotNull ExpressionVisitor visit(@NotNull Consumer<ReferenceValue> consumer) {
        return new ExpressionVisitor() {
            @Override
            protected @NotNull ReferenceValue process(@NotNull ReferenceValue value) {
                consumer.accept(value);
                return value;
            }

            @Override
            protected void update(@NotNull Expression expression) {
                throw new UnsupportedOperationException();
            }
        };
    }

    protected abstract @NotNull ReferenceValue process(@NotNull ReferenceValue value);

    protected abstract void update(@NotNull Expression expression);

    @Override
    public void visitValueExpression(@NotNull ValueExpression expression) {
        Value value = computeValue(expression.value());
        if (!value.equals(expression.value())) {
            update(new ValueExpression(value));
        }
        super.visitValueExpression(expression);
    }

    @Override
    public void visitAggregateExpression(@NotNull AggregateExpression expression) {
        List<Value> values = expression.values().stream()
                .map(this::computeValue).toList();
        if (!values.equals(expression.values())) {
            update(new AggregateExpression(values));
        }
        super.visitAggregateExpression(expression);
    }

    private @NotNull Value computeValue(@NotNull Value value) {
        if (!(value instanceof ReferenceValue referenceValue)) {
            return value;
        }
        return process(referenceValue);
    }

    @Override
    public void visitBinaryExpression(@NotNull BinaryExpression expression) {
        Value left = computeValue(expression.left());
        Value right = computeValue(expression.right());
        if (!left.equals(expression.left()) || !right.equals(expression.right())) {
            update(new BinaryExpression(expression.operator(), left, right));
        }
        super.visitBinaryExpression(expression);
    }

    @Override
    public void visitUnaryExpression(@NotNull UnaryExpression expression) {
        Value value = computeValue(expression.value());
        if (!value.equals(expression.value())) {
            update(new UnaryExpression(expression.operator(), value));
        }
        super.visitUnaryExpression(expression);
    }
}
