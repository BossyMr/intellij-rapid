package com.bossymr.flow.expression;

import com.bossymr.flow.value.type.ValueType;

public class BinaryExpression implements Expression {

    private final BinaryOperator operator;
    private final Expression left;
    private final Expression right;

    public BinaryExpression(BinaryOperator operator, Expression left, Expression right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public static BinaryExpression equals(Expression left, Expression right) {
        return new BinaryExpression(BinaryOperator.Primitive.EQUAL_TO, left, right);
    }

    @Override
    public ValueType getType() {
        return operator.getType();
    }

    public interface BinaryOperator {

        ValueType getType(ValueType left, ValueType right);


        enum Primitive implements BinaryOperator {
            EQUAL_TO {
                @Override
                public ValueType getType(ValueType left, ValueType right) {
                    return ValueType.Primitive.BOOLEAN;
                }
            };
        }
    }
}
