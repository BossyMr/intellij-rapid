package io.github.bossymr.language.psi;

/**
 * Represents a structure.
 */
public interface RapidStructure extends RapidNamedElement, RapidVisibleElement {

    /**
     * Checks if this structure represents an atomic structure.
     *
     * @return if this structure is an atomic structure.
     */
    default boolean isAtomic() {
        return false;
    }

    /**
     * Checks if this structure represents an alias structure.
     *
     * @return if this structure is an alias.
     */
    default boolean isAlias() {
        return false;
    }

    /**
     * Checks if this structure represents a record structure.
     *
     * @return if this structure is a record.
     */
    default boolean isRecord() {
        return false;
    }

}
