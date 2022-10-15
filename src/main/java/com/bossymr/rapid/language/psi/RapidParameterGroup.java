package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a parameter group, which contains a set of mutually exclusive parameters.
 */
public interface RapidParameterGroup extends RapidElement {

    /**
     * Checks if this parameter group is optional.
     *
     * @return if the parameter group is optional.
     */
    boolean isOptional();

    /**
     * Returns the parameters which are declared as a part of this parameter group.
     *
     * @return the parameters of this parameter group.
     */
    @NotNull List<RapidParameter> getParameters();

}
