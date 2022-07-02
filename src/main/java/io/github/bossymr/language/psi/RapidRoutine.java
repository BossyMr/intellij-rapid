package io.github.bossymr.language.psi;

import com.intellij.util.IncorrectOperationException;
import io.github.bossymr.language.psi.type.RapidType;
import io.github.bossymr.language.psi.util.MutableOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a routine.
 */
public interface RapidRoutine extends RapidNamedElement, RapidVisibleElement {

    /**
     * Checks if this routine is a function.
     *
     * @return if this routine is a function.
     */
    default boolean isFunction() {
        return false;
    }

    /**
     * Checks if this routine is a procedure.
     *
     * @return if this routine is a procedure.
     */
    default boolean isProcedure() {
        return false;
    }

    /**
     * Checks if this routine is a trap.
     *
     * @return if this routine is a trap.
     */
    default boolean isTrap() {
        return false;
    }

    /**
     * Returns the return type of this routine.
     *
     * @return the return type of this routine, or {@code null} if this routine is not a function.
     */
    @Nullable RapidType getType();

    /**
     * Sets the return type of this routine to the specified type.
     *
     * @param type the new return type.
     * @throws IncorrectOperationException if the routine type could not be modified.
     */
    void setType(@NotNull RapidType type) throws IncorrectOperationException;

    /**
     * Returns the parameters present in the parameter list of this routine.
     *
     * @return the parameters of this routine, or {@code null} if this routine cannot declare parameters.
     */
    @Nullable List<RapidParameterGroup> getParameters();

    /**
     * Return the fields specified by this routine.
     *
     * @return the fields specified by this routine.
     */
    @NotNull List<RapidField> getFields();

    /**
     * Returns the statements in this routine.
     *
     * @return the statements in this routine.
     */
    @NotNull List<RapidStatement> getStatements();

    /**
     * Returns a modifiable reference to the backward clause of this element.
     *
     * @return a reference to the backward clause of this element, or {@code null} if this routine is not a procedure.
     */
    @Nullable MutableOptional<RapidBackwardClause> getBackwardClause();

    /**
     * Returns a modifiable reference to the undo clause of this element.
     *
     * @return a reference to the undo clause of this element.
     */
    @NotNull MutableOptional<RapidUndoClause> getUndoClause();

    /**
     * Returns a modifiable reference to the error clause of this element.
     *
     * @return a reference to the error clause of this element.
     */
    @NotNull MutableOptional<RapidExceptionClause> getExceptionClause();

}
