package io.github.bossymr.language.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a routine (function, procedure, or trap).
 */
public interface RapidRoutine extends RapidSymbol {

    /**
     * Checks if this routine is only visible inside the module in which it was declared.
     *
     * @return if the routine is marked as local.
     */
    boolean isLocal();

    /**
     * Returns the attributes of which this routine was declared.
     *
     * @return the attribute of this routine.
     */
    @NotNull Attribute getAttribute();

    /**
     * Returns the type of this routine.
     *
     * @return the type of this routine, or {@code null} if this routine is not a function.
     */
    @Nullable RapidType getType();

    /**
     * Returns the type element which declares the type of this routine.
     *
     * @return the type element of this routine, or {@code null} if this routine is not a function.
     */
    @Nullable RapidTypeElement getTypeElement();

    /**
     * Returns the parameter groups of this routine.
     *
     * @return the parameter groups of this routine, or {@code null} if this routine is a trap.
     */
    default @Nullable List<RapidParameterGroup> getParameters() {
        return getParameterList() != null ? getParameterList().getParameters() : null;
    }

    /**
     * Returns the parameter list of this routine.
     *
     * @return the parameter list of this routine, or {@code null} if this routine is a trap.
     */
    @Nullable RapidParameterList getParameterList();

    /**
     * Returns the fields declared in this routine.
     *
     * @return the fields declared in this routine.
     */
    @NotNull List<RapidField> getFields();

    /**
     * Returns the statements contained in this routine.
     *
     * @return the statements contained in this routine.
     */
    default @NotNull List<RapidStatement> getStatements() {
        return getStatementList().getStatements();
    }

    /**
     * Returns the body statement list of this routine.
     *
     * @return the body statement list of this routine.
     */
    @NotNull RapidStatementList getStatementList();


    /**
     * Represents the attributes with which a routine can be declared.
     */
    enum Attribute {
        /**
         * A function executes a set of instructions and returns a value.
         */
        FUNCTION,

        /**
         * A procedure executes a set of instructions.
         */
        PROCEDURE,

        /**
         * A trap is associated with an interrupt.
         */
        TRAP
    }
}
