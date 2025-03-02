package com.bossymr.flow.type;

/**
 * An {@code EmptyType} represents a {@code void} type.
 */
public class EmptyType implements ValueType {

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
        return EmptyType.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EmptyType;
    }

    @Override
    public String toString() {
        return "empty";
    }
}
