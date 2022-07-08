package io.github.bossymr.language.psi;

/**
 * Represents the possible attributes which can be declared on a module.
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
     * The module cannot be modified, but this attribute can be removed.
     */
    READ_ONLY
}
