package com.bossymr.flow.value.type;

/**
 * A {@code ValueType} represents the type of an expression or value.
 */
public interface ValueType {

    /**
     * Checks whether this type represents a structure.
     *
     * @return whether this type represents a structure.
     */
    boolean isStructure();

    /**
     * Checks whether this type represents an array.
     *
     * @return whether this type represents an array.
     */
    boolean isArray();

    /**
     * Create a type representing an array of this type.
     *
     * @return a type representing an array of type.
     */
    default ArrayType createArrayType() {
        return new ArrayType(this);
    }

    /**
     * Returns a boolean type.
     *
     * @return a boolean type.
     */
    static ValueType booleanType() {
        return new BooleanType();
    }

    /**
     * Returns a string type.
     *
     * @return a string type.
     */
    static ValueType stringType() {
        return new StringType();
    }

    /**
     * Returns an unbounded unsigned integer type.
     *
     * @return an unsigned integer type.
     */
    static ValueType unsignedType() {
        return signedType(-1);
    }

    /**
     * Returns a bounded unsigned integer type.
     *
     * @param length the bit-size of the integer.
     * @return an unsigned integer type.
     */
    static ValueType unsignedType(int length) {
        return new IntegerType(false, length);
    }

    /**
     * Returns an unbounded signed integer type.
     *
     * @return a signed integer type.
     */
    static ValueType signedType() {
        return signedType(-1);
    }

    /**
     * Returns a bounded signed integer type.
     *
     * @param length the bit-size of the integer.
     * @return a signed integer type.
     */
    static ValueType signedType(int length) {
        return new IntegerType(true, length);
    }

    /**
     * Returns a floating-point type.
     *
     * @param exponent the bit-size of the exponent.
     * @param significand the bit-size of the significand.
     * @return a floating-point type.
     */
    static ValueType floatType(int exponent, int significand) {
        return new FloatType(exponent, significand);
    }

}
