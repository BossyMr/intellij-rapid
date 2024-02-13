package com.bossymr.rapid.language.symbol;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@code RapidParameterGroup} represents a group of mutually exlusive parameters.
 */
public interface RapidParameterGroup {

    /**
     * Returns the routine in which this parameter group is declared.
     *
     * @return the routine in which this parameter group is declared.
     */
    @NotNull RapidRoutine getRoutine();

    /**
     * Returns whether this parameter group is optional.
     *
     * @return whether this parameter group is optional.
     */
    boolean isOptional();

    /**
     * Returns the parameters in this parameter group.
     *
     * @return the parameters in this parameter group.
     */
    @NotNull List<? extends RapidParameter> getParameters();
}
