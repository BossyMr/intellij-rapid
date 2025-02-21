package com.bossymr.flow.value.type;

import java.util.Objects;

/**
 * An {@code IntegerType} represents an integer value. An  {@code IntegerType} can optionally be fixed to a specific
 * length, which will allow the program to handle overflowing.
 */
public class IntegerType implements ValueType {

    private final boolean signed;
    private final int length;

    /**
     * Create a new {@code IntegerType}.
     *
     * @param signed if this integer is signed.
     * @param length the bit-size of the integer, or {@code -1} to leave the size of this type unspecified.
     */
    public IntegerType(boolean signed, int length) {
        this.signed = signed;
        this.length = length;
    }

    /**
     * Returns whether this integer is signed or unsigned.
     *
     * @return whether this integer is signed.
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * Returns the bit-size of this type.
     *
     * @return the bit-size of this type, or {@code -1} if the size of this type is unspecified.
     */
    public int getLength() {
        return length;
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
        IntegerType that = (IntegerType) o;
        return signed == that.signed && length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hash(signed, length);
    }

    @Override public String toString() {
        return "integer{" + (signed ? "signed" : "unsigned") + (length >= 0 ? length : "") + "}";
    }
}
