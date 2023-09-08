package com.bossymr.rapid.language.flow.condition;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.flow.value.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ExpressionVisitor extends ControlFlowVisitor<Expression> {

    public static @NotNull Expression modify(@NotNull Expression expression, @NotNull Function<ReferenceValue, Value> mapper) {
        return expression.accept(new ExpressionVisitor() {
            @Override
            protected @NotNull Value replace(@NotNull ReferenceValue value) {
                return mapper.apply(value);
            }

        });
    }

    public static void iterate(@NotNull Expression expression, @NotNull Consumer<ReferenceValue> consumer) {
        expression.accept(new ExpressionVisitor() {
            @Override
            protected @NotNull Value replace(@NotNull ReferenceValue value) {
                consumer.accept(value);
                return value;
            }
        });
    }

    protected abstract @NotNull Value replace(@NotNull ReferenceValue value);

    private @NotNull Value computeValue(@NotNull Value value) {
        if (!(value instanceof ReferenceValue referenceValue)) {
            return value;
        }
        return Objects.requireNonNull(replace(referenceValue));
    }

    @Override
    public @NotNull ValueExpression visitValueExpression(@NotNull ValueExpression expression) {
        Value value = computeValue(expression.value());
        if (!value.equals(expression.value())) {
            return new ValueExpression(value);
        }
        return expression;
    }

    @Override
    public @NotNull AggregateExpression visitAggregateExpression(@NotNull AggregateExpression expression) {
        List<Value> values = expression.values().stream()
                .map(this::computeValue).toList();
        if (!values.equals(expression.values())) {
            return new AggregateExpression(values);
        }
        return expression;
    }

    @Override
    public @NotNull BinaryExpression visitBinaryExpression(@NotNull BinaryExpression expression) {
        Value left = computeValue(expression.left());
        Value right = computeValue(expression.right());
        if (!left.equals(expression.left()) || !right.equals(expression.right())) {
            return new BinaryExpression(expression.operator(), left, right);
        }
        return expression;
    }

    @Override
    public @NotNull UnaryExpression visitUnaryExpression(@NotNull UnaryExpression expression) {
        Value value = computeValue(expression.value());
        if (!value.equals(expression.value())) {
            return new UnaryExpression(expression.operator(), value);
        }
        return expression;
    }
}
