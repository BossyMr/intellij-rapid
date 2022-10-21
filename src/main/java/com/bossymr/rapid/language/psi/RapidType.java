package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a type (alias, record, atomic, or array).
 */
public class RapidType {

    private final RapidStructure structure;
    private final int dimensions;

    public RapidType(@Nullable RapidStructure structure) {
        this(structure, 0);
    }

    public RapidType(@Nullable RapidStructure structure, int dimensions) {
        this.structure = structure;
        this.dimensions = dimensions;
    }

    /**
     * Checks if a value of the specified {@code right} type can be assigned to a field of {@code left} type.
     *
     * @param left  the type to assign to.
     * @param right the value type.
     * @return if the right type can be assigned to a field of the left type.
     */
    public static boolean isAssignable(@NotNull RapidType left, @NotNull RapidType right) {
        if (left.equals(right)) return true;
        if (left.getDimensions() != right.getDimensions()) return false;
        RapidStructure leftStructure = unwrap(left);
        RapidStructure rightStructure = unwrap(right);
        if (leftStructure == null || rightStructure == null) return false;
        return Objects.equals(leftStructure, rightStructure);
    }

    private static @Nullable RapidStructure unwrap(@NotNull RapidType type) {
        RapidStructure structure = type.getStructure();
        while (structure instanceof RapidAlias) {
            RapidType raw = ((RapidAlias) structure).getType();
            structure = raw != null ? raw.getStructure() : null;
        }
        return structure;
    }

    /**
     * Creates a new array type with the specified dimensions and the same type as this type.
     *
     * @param dimensions the dimensions of the new type.
     * @return a new array type.
     */
    @Contract(pure = true)
    public @NotNull RapidType createArrayType(int dimensions) {
        return new RapidType(getStructure(), dimensions);
    }

    /**
     * Returns the structure of this type.
     *
     * @return the structure of this type.
     */
    public @Nullable RapidStructure getStructure() {
        return structure;
    }

    /**
     * Returns the dimensions of this structure, a dimension of 0 represents a non-array type.
     *
     * @return the dimensions of this structure.
     */
    public int getDimensions() {
        return dimensions;
    }

    /**
     * Returns the name of the structure of this type.
     *
     * @return the name of the structure of this type.
     */
    public @Nullable String getCanonicalText() {
        return getStructure() != null ? getStructure().getName() : null;
    }

    /**
     * Returns a presentable representation of this type.
     *
     * @return a presentable representation of this type.
     */
    public @NotNull String getPresentableText() {
        StringBuilder builder = new StringBuilder();
        builder.append(getCanonicalText());
        if(getDimensions() > 0) {
            builder.append('{');
            for (int i = 0; i < dimensions; i++) {
                if (i > 0) builder.append(",");
                builder.append('*');
            }
            builder.append('}');
        }
        return builder.toString();
    }

    /**
     * Checks if a value of the specified type can be assigned to this type.
     *
     * @param type the value type.
     * @return if the specified type can be assigned to a field of this type.
     */
    public boolean isAssignable(@NotNull RapidType type) {
        return isAssignable(this, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidType rapidType = (RapidType) o;
        return getDimensions() == rapidType.getDimensions() && Objects.equals(getStructure(), rapidType.getStructure());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getStructure(), getDimensions());
    }

    @Override
    public String toString() {
        return "RapidType:" + getPresentableText();
    }
}
