package io.github.bossymr.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidParameterList extends RapidElement {

    /**
     * Returns the parameter groups in this list.
     *
     * @return the parameter groups in this list.
     */
    @NotNull List<RapidParameterGroup> getParameters();

}
