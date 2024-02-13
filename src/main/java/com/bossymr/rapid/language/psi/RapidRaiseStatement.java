package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a raise statement.
 */
public interface RapidRaiseStatement extends RapidStatement {

    /**
     * Returns the value which is raised by this statement.
     *
     * @return the value which is raised by this statement, or {@code null} if no value is raised.
     */
    @Nullable RapidExpression getExpression();

}
