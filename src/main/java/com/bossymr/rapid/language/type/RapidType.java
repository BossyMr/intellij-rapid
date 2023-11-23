package com.bossymr.rapid.language.type;

import com.bossymr.rapid.language.symbol.RapidStructure;
import com.bossymr.rapid.language.symbol.ValueType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code RapidType} represents the type of value a variable or expression can represent.
 */
public interface RapidType {

    /**
     * Checks whether a value of the specified type can be assigned to a variable or expression of this type.
     *
     * @param type the type.
     * @return whether the specified type is assignable to this type.
     */
    @Contract(pure = true)
    default boolean isAssignable(@NotNull RapidType type) {
        if(type.equals(this)) {
            return true;
        }
        if(getDimensions() != type.getDimensions()) {
            return false;
        }
        RapidStructure thisStructure = getActualStructure();
        RapidStructure otherStructure = type.getActualStructure();
        if(thisStructure == null || otherStructure == null) {
            return getText().equals(type.getText());
        }
        return thisStructure.equals(otherStructure);
    }

    /**
     * Creates a new type which represents an array, of the specified degree, of this type.
     *
     * @param dimensions the degree of the array.
     * @return a new type which represents an array of this type.
     * @throws IllegalArgumentException if the specified degree is negative.
     */
    default @NotNull RapidType createArrayType(int dimensions) {
        if(dimensions == 0) {
            return this;
        }
        if(dimensions < 0) {
            throw new IllegalArgumentException("Cannot create array type with degree: " + dimensions);
        }
        RapidType type = new RapidArrayType(this);
        for (int i = 1; i < dimensions; i++) {
            type = new RapidArrayType(type);
        }
        return type;
    }

    /**
     * Returns the number of dimensions of this type.
     *
     * @return the number of dimensions of this type.
     */
    default int getDimensions() {
        return 0;
    }

    /**
     * Returns the underlying structure of this type.
     *
     * @return the structure of this type.
     * @see #getActualStructure()
     */
    @Nullable RapidStructure getStructure();

    /**
     * Returns the underlying structure of this type. If the structure is an alias to another type, the structure to
     * which the alias resolves to is returned instead. Likewise, if the structure is an atomic, and is a semi-value
     * type, the underlying type is returned instead.
     *
     * @return the structure of this type.
     */
    @Nullable RapidStructure getActualStructure();

    /**
     * Returns the value type of this type, which represents whether this type can be represented, either wholly or
     * partially as a constant.
     *
     * @return the value type of this type.
     */
    @NotNull ValueType getValueType();

    /**
     * Returns the name of this type.
     *
     * @return the name of this type.
     */
    @NotNull String getText();

    /**
     * Returns a presentable textual representation of this type.
     *
     * @return a textual representation of this type.
     */
    default @NotNull String getPresentableText() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getText());
        if (getDimensions() > 0) {
            stringBuilder.append("{");
            for (int i = 0; i < getDimensions(); i++) {
                if (i > 0) stringBuilder.append(",");
                stringBuilder.append("*");
            }
            stringBuilder.append("}");
        }
        return stringBuilder.toString();    }
}
