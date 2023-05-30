package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a procedure call statement.
 */
public interface RapidProcedureCallStatement extends RapidStatement {

    /**
     * Checks if the procedure is bound late to the value of the specified expression, or if it directly references the
     * procedure.
     *
     * @return if the procedure is bound late.
     */
    boolean isLate();

    /**
     * Returns the expression referencing the procedure, or if the procedure reference is bound late, an expression
     * resulting in a string type which is equal to the name of the procedure.
     *
     * @return the expression referencing the procedure.
     */
    @NotNull RapidExpression getReferenceExpression();

    /**
     * Returns the argument list with the parameters of the procedure call.
     *
     * @return the argument list of this procedure call.
     */
    @NotNull RapidArgumentList getArgumentList();

}
