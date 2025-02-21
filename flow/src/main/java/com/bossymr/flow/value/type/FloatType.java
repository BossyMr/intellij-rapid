package com.bossymr.flow.value.type;

import java.util.Objects;

/**
 * A {@code FloatType} represents a floating-point type. Unlike a {@link RealType}, a floating-point type is subject to
 * inaccuracies associated with floating-point values.
 */
public class FloatType implements ValueType {

    private final int exponent;
    private final int significand;

    /**
     * Create a new {@code FloatType}.
     *
     * @param exponent the bit-size of the exponent.
     * @param significand the bit-size of the significand.
     */
    public FloatType(int exponent, int significand) {
        this.exponent = exponent;
        this.significand = significand;
    }

    public int getExponent() {
        return exponent;
    }

    public int getSignificand() {
        return significand;
    }

    @Override
    public boolean isStructure() {
        return false;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FloatType floatType = (FloatType) o;
        return exponent == floatType.exponent && significand == floatType.significand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(exponent, significand);
    }

    @Override
    public String toString() {
        return "float{" + exponent + ", " + significand + "}";
    }
}
