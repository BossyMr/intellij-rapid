package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Represents a symbol which is a module.
 */
public interface RapidModule extends RapidSymbol {

    /**
     * Returns the attribute list, containing the attributes which this module is declared with.
     *
     * @return the attribute list of this module, or {@code null} if no attributes are declared.
     * @see #getAttributes()
     */
    @NotNull RapidAttributeList getAttributeList();

    /**
     * Returns the attributes which this module is declared with, effectively the same as
     * {@link RapidAttributeList#getAttributes()}.
     *
     * @return the attributes which this module is declared with.
     */
    @NotNull Set<ModuleAttribute> getAttributes();

    /**
     * Checks if the specified attribute is declared by this module, effectively the same as
     * {@link RapidAttributeList#hasAttribute(ModuleAttribute)}.
     *
     * @return if the specified attribute is declared by this module.
     */
    boolean hasAttribute(ModuleAttribute attribute);

    /**
     * Attempts to add or remove the specified attribute to this attribute list, effectively the same as
     * {@link RapidAttributeList#setAttribute(ModuleAttribute, boolean)}.
     *
     * @param attribute the attribute to add or remove.
     * @param value     {@code true} to add the attribute, and {@code false} to remove the attribute.
     * @throws UnsupportedOperationException if the attribute list could not be modified.
     */
    void setAttribute(ModuleAttribute attribute, boolean value) throws UnsupportedOperationException;

    /**
     * Returns the structures declared in this module.
     *
     * @return a list of structures.
     */
    @NotNull List<RapidStructure> getStructures();

    /**
     * Returns the fields declared in this module.
     *
     * @return a list of fields.
     */
    @NotNull List<RapidField> getFields();

    /**
     * Returns the fields declared in this module.
     *
     * @return a list of routines.
     */
    @NotNull List<RapidRoutine> getRoutines();
}
