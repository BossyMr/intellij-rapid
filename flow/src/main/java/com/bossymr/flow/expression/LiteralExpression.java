package com.bossymr.flow.expression;

import com.bossymr.flow.value.type.ValueType;

public class LiteralExpression implements Expression {

    private final ValueType type;
    private final Object value;

    public LiteralExpression(Object value) {
        this.type = switch (value) {
            case null -> null;
            default -> throw new IllegalStateException("Unexpected value: " + value);
        };
        this.value = value;
    }

    @Override
    public ValueType getType() {
        return null;
    }
}
