package com.bossymr.flow.type;

/**
 * A {@code RealType} perfectly represents any numeric value.
 */
public class RealType implements NumericType {

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

    /**
     * A {@code Fraction} represents a real number.
     *
     * @param numerator the numerator.
     * @param denominator the denominator.
     */
    public record Fraction(long numerator, long denominator) {}
}
