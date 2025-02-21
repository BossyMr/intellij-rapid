package com.bossymr.flow.value;

import com.bossymr.flow.type.ValueType;

import java.util.Objects;

/**
 * A {@code Variable} represents a variable which can be assigned a value.
 */
public class Variable implements Value {

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
     * Returns the name of the variable.
     *
     * @return the name of the variable.
     */
    public String getName() {
        return name;
    }

    @Override
    public ValueType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Variable variable = (Variable) o;
        return Objects.equals(name, variable.name) && Objects.equals(type, variable.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Variable{" +
               "name='" + name + '\'' +
               ", type=" + type +
               '}';
    }
}
