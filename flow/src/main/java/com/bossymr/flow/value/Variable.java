package com.bossymr.flow.value;

import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.value.type.ValueType;

/**
 * A {@code Variable} represents a variable which can be assigned a value.
 */
public class Variable implements Value, Expression {

    private final ValueType type;

    public Variable(ValueType type) {
        this.type = type;
    }

    @Override
    public ValueType getType() {
        return type;
    }
}
