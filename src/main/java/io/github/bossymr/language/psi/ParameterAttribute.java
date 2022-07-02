package io.github.bossymr.language.psi;

/**
 * Represents the different attributes which a parameter can be declared as.
 */
public enum ParameterAttribute {

    /**
     * The parameter is initialized to the value of the routine argument, but can be reassigned as a regular field.
     */
    INPUT,

    /**
     * The parameter is used as an alias to the routine argument, and any changes update the argument value.
     */
    VARIABLE,

    /**
     * The parameter is used as an alias to the routine argument, and any changes update the argument value.
     */
    PERSISTENT,

    /**
     * The parameter is used as an alias to the routine argument, and any changes update the argument value.
     */
    INPUT_OUTPUT,

    /**
     * The parameter is used as an alias to the routine argument, and any changes update the argument value.
     */
    REFERENCE
}
