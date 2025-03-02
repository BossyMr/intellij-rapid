package com.bossymr.flow.expression;

import com.bossymr.flow.type.BooleanType;
import com.bossymr.flow.type.IntegerType;
import com.bossymr.flow.type.NumericType;
import com.bossymr.flow.type.ValueType;

import java.util.function.Function;

/**
 * A {@code BinaryExpression} represents a binary expression.
 */
public class BinaryExpression implements Expression {

    private final Operator operator;
    private final ValueType type;
    private final Expression left;
    private final Expression right;

    /**
     * Create a new {@code BinaryExpression}.
     *
     * @param operator the operator.
     * @param left the expression to the left of the operator.
     * @param right the expression to the right of the operator.
     * @throws IllegalArgumentException if the expression is not valid.
     */
    public BinaryExpression(Operator operator, Expression left, Expression right) {
        ValueType type = operator.getType(left.getType(), right.getType());
        if (type == null) {
            throw new IllegalArgumentException("binary expression '" + left + " " + operator + " " + right + "' is not valid");
        }
        this.operator = operator;
        this.type = type;
        this.left = left;
        this.right = right;
    }

    @Override
    public ValueType getType() {
        return type;
    }

    @Override
    public Expression translate(Function<Expression, Expression> mapper) {
        Expression self = mapper.apply(this);
        if (self != this) {
            return self;
        }
        Expression left = mapper.apply(this.left);
        Expression right = mapper.apply(this.right);
        if (left != this.left || right != this.right) {
            return new BinaryExpression(operator, left, right);
        }
        return this;
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
     * Returns the expression to the left of the operator.
     *
     * @return the expression to the left of the operator.
     */
    public Expression getLeft() {
        return left;
    }

    /**
     * Returns the expression to the right of the operator.
     *
     * @return the expression to the right of the operator.
     */
    public Expression getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "(" + getLeft() + " " + getOperator() + " " + getRight() + ")";
    }

    /**
     * An {@code Operator} represents a binary operator.
     */
    public enum Operator {
        EQUAL_TO("=") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'EQUAL_TO' can be applied to any two expressions.
                return ValueType.booleanType();
            }
        },
        GREATER_THAN(">") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'GREATER_THAN' can be applied to any two numeric expressions.
                if (!(left instanceof NumericType) || !(right instanceof NumericType)) {
                    return null;
                }
                return ValueType.booleanType();
            }
        },
        LESS_THAN("<") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'LESS_THAN' can be applied to any two numeric expressions.
                if (!(left instanceof NumericType) || !(right instanceof NumericType)) {
                    return null;
                }
                return ValueType.booleanType();
            }
        },
        ADD("+") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'ADD' can be applied to any two numeric expressions.
                if (!(left instanceof NumericType) || !(right instanceof NumericType)) {
                    return null;
                }
                return left;
            }
        },
        SUBTRACT("+") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'SUBTRACT' can be applied to any two numeric expressions.
                if (!(left instanceof NumericType) || !(right instanceof NumericType)) {
                    return null;
                }
                return left;
            }
        },
        MULTIPLY("*") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'MULTIPLY' can be applied to any two numeric expressions.
                if (!(left instanceof NumericType) || !(right instanceof NumericType)) {
                    return null;
                }
                return left;
            }
        },
        DIVIDE("/") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'DIVIDE' can be applied to any two numeric expressions.
                if (!(left instanceof NumericType) || !(right instanceof NumericType)) {
                    return null;
                }
                return left;
            }
        },
        MODULO("%") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'MODULO' can be applied to any two integer expressions.
                if (!(left instanceof IntegerType) || !(right instanceof IntegerType)) {
                    return null;
                }
                return left;
            }
        },
        AND("AND") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'AND' can be applied to any two boolean expressions.
                if (!(left instanceof BooleanType) || !(right instanceof BooleanType)) {
                    return null;
                }
                return ValueType.booleanType();
            }
        },
        XOR("XOR") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'XOR' can be applied to any two boolean expressions.
                if (!(left instanceof BooleanType) || !(right instanceof BooleanType)) {
                    return null;
                }
                return ValueType.booleanType();
            }
        },
        OR("OR") {
            @Override
            ValueType getType(ValueType left, ValueType right) {
                // 'OR' can be applied to any two boolean expressions.
                if (!(left instanceof BooleanType) || !(right instanceof BooleanType)) {
                    return null;
                }
                return ValueType.booleanType();
            }
        };

        private final String name;

        Operator(String name) {
            this.name = name;
        }

        /**
         * Returns the type of an expression with this operator.
         *
         * @param left the type of the expression to the left of this operator.
         * @param right the type of the expression to the right of this operator.
         * @return the type of an expression with this operator, or {@code null} if the expression isn't valid.
         */
        abstract ValueType getType(ValueType left, ValueType right);

        @Override
        public String toString() {
            return name;
        }
    }
}
