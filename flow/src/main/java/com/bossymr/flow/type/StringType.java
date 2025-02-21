package com.bossymr.flow.type;

/**
 * A {@code StringType} represents a string type.
 */
public class StringType implements ValueType {

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
        return StringType.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StringType;
    }

    @Override
    public String toString() {
        return "string";
    }
}
