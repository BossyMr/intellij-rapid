package com.bossymr.rapid.language.psi;

import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface RapidParameterList extends RapidElement {

    /**
     * Returns the parameter groups in this list.
     *
     * @return the parameter groups in this list.
     */
    @NotNull List<PhysicalParameterGroup> getParameters();

}
