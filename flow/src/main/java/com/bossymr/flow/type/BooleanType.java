package com.bossymr.flow.type;

/**
 * A {@code BooleanType} represents a boolean type.
 */
public class BooleanType implements ValueType {

    @Override
    public boolean isStructure() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public int hashCode() {
        return BooleanType.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BooleanType;
    }

    @Override
    public String toString() {
        return "boolean";
    }
}
