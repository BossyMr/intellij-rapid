package com.bossymr.flow.state;

import com.bossymr.flow.expression.Expression;
import com.bossymr.flow.type.ValueType;

import java.util.function.Function;

public final class Variable implements Expression {

    private final String name;
    private final ValueType type;

    /**
     * Create a new {@code Variable}.
     *
     * @param name the name of the variable.
     * @param type the type of the variable.
     */
    public Variable(String name, ValueType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * Returns the name of this variable.
     * @return the name of this variable.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this snapshot.
     *
     * @return the type of this snapshot.
     */
    public ValueType getType() {
        return type;
    }

    @Override
    public Expression translate(Function<Expression, Expression> mapper) {
        return mapper.apply(this);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "{" + name + "}";
    }
}
