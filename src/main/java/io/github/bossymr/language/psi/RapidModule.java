package io.github.bossymr.language.psi;

import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

/**
 * Represents a module.
 *
 * @see RapidFile#getModules()
 */
public interface RapidModule extends RapidNamedElement {

    /**
     * Returns the attributes set by this module.
     *
     * @return a set of unique module attributes.
     */
    @NotNull Set<ModuleAttribute> getAttributes();

    /**
     * Checks if the specified attribute is set by this module.
     *
     * @return if the specified attribute is set by this module.
     */
    boolean hasAttribute(@NotNull ModuleAttribute attribute);

    /**
     * Adds, or removes, the specified attribute for this module.
     *
     * @param attribute the attribute to add or remove.
     * @param value     {@code true} to set the attribute, {@code false} to remove the attribute.
     * @throws IncorrectOperationException if the attribute could not be modified.
     */
    void setAttribute(@NotNull ModuleAttribute attribute, boolean value) throws IncorrectOperationException;

    /**
     * Returns the structures declared in this module.
     *
     * @return a list of structures declared in this module.
     */
    @NotNull List<RapidStructure> getStructures();

    /**
     * Returns the fields declared in this module.
     *
     * @return a list of fields declared in this module.
     */
    @NotNull List<RapidField> getFields();

    /**
     * Returns the routines declared in this module.
     *
     * @return a list of routines declared in this module.
     */
    @NotNull List<RapidRoutine> getRoutines();

}
