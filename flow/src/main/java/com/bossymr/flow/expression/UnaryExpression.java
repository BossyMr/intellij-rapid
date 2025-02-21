package com.bossymr.flow.expression;

import com.bossymr.flow.type.BooleanType;
import com.bossymr.flow.type.NumericType;
import com.bossymr.flow.type.ValueType;

/**
 * A {@code UnaryExpression} represents a unary expression.
 */
public class UnaryExpression implements Expression {

    private final Operator operator;
    private final ValueType type;
    private final Expression expression;

    /**
     * Create a new {@code UnaryExpression}.
     *
     * @param operator the operator.
     * @param expression the expression.
     */
    public UnaryExpression(Operator operator, Expression expression) {
        ValueType type = operator.getType(expression.getType());
        if (type == null) {
            throw new IllegalArgumentException("unary expression '" + operator + " " + expression + "' is not valid");
        }
        this.operator = operator;
        this.type = type;
        this.expression = expression;
    }

    @Override
    public ValueType getType() {
        return type;
    }

    /**
     * Returns the operator of this expression.
     *
     * @return the operator of this expression.
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Returns the expression to the right of this expression.
     *
     * @return the expression to the right of this expression.
     */
    public Expression getExpression() {
        return expression;
    }

    /**
     * An {@code Operator} represents a unary operator.
     */
    public enum Operator {
        NOT("!") {
            @Override
            ValueType getType(ValueType type) {
                // 'NOT' can only be applied to a boolean expression.
                if (!(type instanceof BooleanType)) {
                    return null;
                }
                return ValueType.booleanType();
            }
        },
        NEGATE("-") {
            @Override
            ValueType getType(ValueType type) {
                // 'NEGATE' can only be applied to a numeric expression.
                // Observe that different operations are used depending on if the type is a floating-point or an
                // integer. However, as this is dependent on the solver, this change is done at a later point in the
                // program.
                if (!(type instanceof NumericType)) {
                    return null;
                }
                return type;
            }
        };

        private final String name;

        Operator(String name) {
            this.name = name;
        }

        /**
         * Returns the type of an expression with this operator.
         *
         * @param type the type of the expression to the right of this expression.
         * @return the type of an expression with this operator, or {@code null} if this expression isn't valid.
         */
        abstract ValueType getType(ValueType type);

        @Override
        public String toString() {
            return name;
        }
    }
}
