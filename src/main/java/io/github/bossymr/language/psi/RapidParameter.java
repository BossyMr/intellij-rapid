package io.github.bossymr.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a parameter.
 */
public interface RapidParameter extends RapidSymbol {

    /**
     * Returns the attribute with which this parameter was declared.
     *
     * @return the attribute of this parameter.
     */
    @NotNull Attribute getAttribute();

    /**
     * Returns the type of this parameter.
     *
     * @return the type of this parameter, or {@code null} if the parameter is incomplete.
     */
    @Nullable RapidType getType();

    /**
     * Returns the type element of this parameter.
     *
     * @return the type element of this parameter, or {@code null} if the parameter is incomplete.
     */
    @Nullable RapidTypeElement getTypeElement();

    /**
     * Represents the attributes of a parameter.
     */
    enum Attribute {
        /**
         * The parameter is initialized to the value of the argument, but can be reassigned to another value and used as
         * a regular variable. This is the default attribute of a parameter.
         */
        INPUT,

        /**
         * A variable parameter can be initialized to a variable, and any changes to the parameter updates the argument
         * variable.
         */
        VARIABLE,

        /**
         * A persistent parameter can be initialized to a persistent, and any changes to the parameter updates the
         * argument persistent.
         */
        PERSISTENT,

        /**
         * An input-output parameter can be initialized to any field, and any changes update the argument field.
         */
        INOUT,

        /**
         * This parameter can be initialized to any value. This attribute can only be used by predefined routines.
         */
        REFERENCE
    }

}
