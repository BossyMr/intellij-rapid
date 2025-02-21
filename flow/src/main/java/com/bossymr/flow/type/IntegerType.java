package com.bossymr.flow.type;

/**
 * An {@code IntegerType} represents an integer value.
 */
public class IntegerType implements NumericType {

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
        return IntegerType.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IntegerType;
    }

    @Override public String toString() {
        return "integer";
    }
}
