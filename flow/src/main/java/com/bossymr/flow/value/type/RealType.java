package com.bossymr.flow.value.type;

/**
 * A {@code RealType} perfectly represents any numeric value. Unlike a {@link FloatType} or {@link IntegerType}, a real
 * type is not fixed to a specific length.
 */
public class RealType implements ValueType {

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
        return RealType.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RealType;
    }

    @Override
    public String toString() {
        return "real";
    }
}
