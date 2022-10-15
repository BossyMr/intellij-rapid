package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Represents an attribute list connected to a module.
 *
 * @see RapidModule#getAttributeList()
 */
public interface RapidAttributeList extends RapidElement {

    /**
     * Returns the attributes declared in this attribute list.
     *
     * @return the attributes declared in this attribute list.
     * @see RapidModule#getAttributes()
     */
    @NotNull Set<ModuleAttribute> getAttributes();

    /**
     * Checks if the specified attribute is declared in this attribute list.
     *
     * @return if the specified attribute is declared in attribute list.
     */
    boolean hasAttribute(ModuleAttribute attribute);

    /**
     * Attempts to add or remove the specified attribute to this attribute list.
     *
     * @param attribute the attribute to add or remove.
     * @param value     {@code true} to add the attribute, and {@code false} to remove the attribute.
     * @throws UnsupportedOperationException if the attribute list could not be modified
     */
    void setAttribute(ModuleAttribute attribute, boolean value) throws UnsupportedOperationException;

}
