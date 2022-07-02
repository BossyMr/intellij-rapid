package io.github.bossymr.language.psi;

/**
 * Represents a named element that is optionally global, or explicitly marked as local.
 */
public interface RapidVisibleElement extends RapidNamedElement {

    /**
     * Checks if this element is declared as a local symbol.
     *
     * @return if this element is local.
     */
    boolean isLocal();

}
