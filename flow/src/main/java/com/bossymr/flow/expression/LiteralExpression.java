package com.bossymr.flow.expression;

import com.bossymr.flow.type.RealType;
import com.bossymr.flow.type.ValueType;

import java.util.function.Function;

/**
 * A {@code LiteralExpression} represents a literal value.
 */
public class LiteralExpression implements Expression {

    private final ValueType type;
    private final Object value;

    private LiteralExpression(ValueType type, Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Create a new {@code LiteralExpression} with the provided boolean value.
     *
     * @param value the value.
     * @return a new {@code LiteralExpression}.
     */
    public static LiteralExpression booleanLiteral(boolean value) {
        return new LiteralExpression(ValueType.booleanType(), value);
    }

    /**
     * Create a new {@code LiteralExpression} with the provided string value.
     *
     * @param value the value.
     * @return a new {@code LiteralExpression}.
     */
    public static LiteralExpression stringLiteral(String value) {
        return new LiteralExpression(ValueType.stringType(), value);
    }

    /**
     * Create a new {@code LiteralExpression} with the provided numeric value.
     *
     * @param value the value.
     * @return a new {@code LiteralExpression}.
     */
    public static LiteralExpression numericLiteral(long value) {
        return new LiteralExpression(ValueType.numericType(), new RealType.Fraction(value, 1));
    }

    /**
     * Create a new {@code LiteralExpression} with the provided numeric value.
     *
     * @param numerator the numerator.
     * @param denominator the denominator.
     * @return a new {@code LiteralExpression}.
     */
    public static LiteralExpression numericLiteral(long numerator, long denominator) {
        return new LiteralExpression(ValueType.numericType(), new RealType.Fraction(numerator, denominator));
    }

    /**
     * Create a new {@code LiteralExpression} with the provided integer value.
     *
     * @param value the value.
     * @return a new {@code LiteralExpression}.
     */
    public static LiteralExpression integerLiteral(long value) {
        return new LiteralExpression(ValueType.integerType(), value);
    }

    @Override
    public ValueType getType() {
        return type;
    }

    @Override
    public Expression translate(Function<Expression, Expression> mapper) {
        return mapper.apply(this);
    }

    /**
     * Returns the literal value of this expression.
     *
     * @return the literal value of this expression.
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        return value.toString();
    }
}
