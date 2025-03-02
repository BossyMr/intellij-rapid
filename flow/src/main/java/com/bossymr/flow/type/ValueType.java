package com.bossymr.flow.type;

/**
 * A {@code ValueType} represents the type of an expression or value.
 */
public interface ValueType {

    /**
     * Returns an empty type.
     *
     * @return an empty type.
     */
    static ValueType emptyType() {
        return new EmptyType();
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
     * Returns a numeric type.
     *
     * @return a numeric type.
     */
    static ValueType numericType() {
        return new RealType();
    }

    /**
     * Returns an integer type.
     *
     * @return an integer type.
     */
    static ValueType integerType() {
        return new IntegerType();
    }

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
}
