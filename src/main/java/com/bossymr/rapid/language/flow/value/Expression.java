package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * An expression represents an expression.
 */
public interface Expression {

    /**
     * Returns the type for this expression.
     *
     * @return the type for this expression.
     */
    @NotNull RapidType getType();

    <R> R accept(@NotNull ControlFlowVisitor<R> visitor);

    default @NotNull Expression replace(@NotNull Function<Expression, Expression> mapper) {
        return accept(new ControlFlowVisitor<>() {
            @Override
            public Expression visitAggregateExpression(@NotNull AggregateExpression expression) {
                return mapper.apply(new AggregateExpression(expression.getType(), expression.getComponents().stream().map(mapper).toList()));
            }

            @Override
            public Expression visitBinaryExpression(@NotNull BinaryExpression expression) {
                return mapper.apply(new BinaryExpression(expression.getOperator(), expression.getType(), mapper.apply(expression.getLeft()), mapper.apply(expression.getRight())));
            }

            @Override
            public Expression visitUnaryExpression(@NotNull UnaryExpression expression) {
                return mapper.apply(expression.replace(mapper));
            }

            @Override
            public Expression visitReferenceExpression(@NotNull ReferenceExpression expression) {
                return mapper.apply(expression);
            }

            @Override
            public Expression visitExpression(@NotNull Expression expression) {
                throw new UnsupportedOperationException();
            }
        });
    }

    default @NotNull Collection<Expression> getAllComponents() {
        Set<Expression> visited = new HashSet<>();
        Deque<Expression> queue = new ArrayDeque<>(getComponents());
        while (!(queue.isEmpty())) {
            Expression expression = queue.removeFirst();
            if (!(visited.add(expression))) {
                continue;
            }
            queue.addAll(expression.getComponents());
        }
        return visited;
    }

    default @NotNull Collection<Expression> getComponents() {
        return accept(new ControlFlowVisitor<>() {
            @Override
            public Collection<Expression> visitAggregateExpression(@NotNull AggregateExpression expression) {
                return expression.getComponents();
            }

            @Override
            public Collection<Expression> visitBinaryExpression(@NotNull BinaryExpression expression) {
                return List.of(expression.getLeft(), expression.getRight());
            }

            @Override
            public Collection<Expression> visitUnaryExpression(@NotNull UnaryExpression expression) {
                return List.of(expression.getExpression());
            }

            @Override
            public Collection<Expression> visitReferenceExpression(@NotNull ReferenceExpression expression) {
                return List.of(expression);
            }

            @Override
            public Collection<Expression> visitExpression(@NotNull Expression expression) {
                throw new UnsupportedOperationException();
            }
        });
    }

}
