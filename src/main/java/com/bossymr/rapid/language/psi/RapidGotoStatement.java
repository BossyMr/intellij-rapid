package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a goto statement.
 */
public interface RapidGotoStatement extends RapidStatement {

    /**
     * Returns the reference expression referencing the destination label.
     *
     * @return the reference expression of this statement, or {@code null} if this statement is incomplete.
     */
    @Nullable RapidReferenceExpression getReferenceExpression();

}
