package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.Nullable;

public interface RapidAssignmentStatement extends RapidStatement, RapidExpression {

    /**
     * Returns the expression to the left of the assignment.
     *
     * @return the left expression, or {@code null} if the left side is represented by {@code <VAR>}.
     */
    @Nullable RapidExpression getLeft();

    /**
     * Returns the expression to the right of the assignment.
     * @return the right expression, or {@code null} if the statement is incomplete.
     */
    @Nullable RapidExpression getRight();

}
