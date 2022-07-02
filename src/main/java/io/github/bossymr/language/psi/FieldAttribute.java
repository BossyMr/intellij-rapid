package io.github.bossymr.language.psi;

/**
 * Represents the different attributes which a field can be declared as.
 */
public enum FieldAttribute {
    /**
     * The field represents a regular variable.
     */
    VARIABLE,

    /**
     * The field value is persisted during shutdown and initialized to its previous value.
     */
    PERSISTENT,

    /**
     * The field value cannot be modified.
     */
    CONSTANT
}
