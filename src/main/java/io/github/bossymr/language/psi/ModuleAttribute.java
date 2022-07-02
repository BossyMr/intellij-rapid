package io.github.bossymr.language.psi;

/**
 * Represents the different attributes which a module can declare.
 */
public enum ModuleAttribute {
    /**
     * The module is a system module.
     */
    SYSTEM_MODULE,

    /**
     * The module source code is not viewable.
     */
    NO_VIEW,

    /**
     * The module cannot be stepped into during stepwise execution.
     */
    NO_STEP_IN,

    /**
     * The module cannot be modified.
     */
    VIEW_ONLY,

    /**
     * The module cannot be modified, but the attribute can be removed.
     */
    READ_ONLY
}
