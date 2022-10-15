package com.bossymr.rapid.language.psi;

/**
 * Represents a structure (atomic, alias, or record).
 */
public interface RapidStructure extends RapidSymbol {

    /**
     * Checks if this structure is only visible inside the module in which it was declared.
     *
     * @return if this structure is local.
     */
    boolean isLocal();
}
