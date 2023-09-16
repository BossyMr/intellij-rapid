package com.bossymr.rapid.language.flow.value;

import com.bossymr.rapid.language.flow.ControlFlowVisitor;
import com.bossymr.rapid.language.psi.RapidExpression;
import com.bossymr.rapid.language.type.RapidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Nullable RapidExpression getElement();

    default @NotNull Expression replace(@NotNull Function<Expression, Expression> mapper) {
        return accept(new ControlFlowVisitor<>() {
            @Override
            public Expression visitAggregateExpression(@NotNull AggregateExpression expression) {
                List<Expression> expressions = expression.getComponents().stream()
                        .map(component -> component.accept(this))
                        .toList();
                AggregateExpression aggregateExpression = new AggregateExpression(expression.getType(), expressions);
                return mapper.apply(aggregateExpression);
            }

            @Override
            public Expression visitBinaryExpression(@NotNull BinaryExpression expression) {
                Expression left = expression.getLeft().accept(this);
                Expression right = expression.getRight().accept(this);
                BinaryExpression binaryExpression = new BinaryExpression(expression.getOperator(), left, right);
                return mapper.apply(binaryExpression);
            }

            @Override
            public Expression visitUnaryExpression(@NotNull UnaryExpression expression) {
                Expression component = expression.accept(this);
                UnaryExpression unaryExpression = new UnaryExpression(expression.getType(), expression.getOperator(), component);
                return mapper.apply(unaryExpression);
            }

            @Override
            public Expression visitComponentExpression(@NotNull ComponentExpression expression) {
                ReferenceExpression variable = (ReferenceExpression) expression.getVariable().accept(this);
                ComponentExpression componentExpression = new ComponentExpression(expression.getType(), variable, expression.getComponent());
                return super.visitComponentExpression(componentExpression);
            }

            @Override
            public Expression visitIndexExpression(@NotNull IndexExpression expression) {
                ReferenceExpression variable = (ReferenceExpression) expression.getVariable().accept(this);
                Expression index = expression.getIndex().accept(this);
                IndexExpression indexExpression = new IndexExpression(variable, index);
                return super.visitIndexExpression(indexExpression);
            }

            @Override
            public Expression visitExpression(@NotNull Expression expression) {
                return mapper.apply(expression);
            }
        });
    }

    default @NotNull Collection<Expression> getComponents() {
        Set<Expression> expressions = new HashSet<>();
        replace(expression -> {
            expressions.add(expression);
            return expression;
        });
        return expressions;
    }

}
