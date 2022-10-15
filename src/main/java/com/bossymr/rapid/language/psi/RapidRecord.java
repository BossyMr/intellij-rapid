package com.bossymr.rapid.language.psi;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Represents a record.
 */
public interface RapidRecord extends RapidStructure {

    /**
     * Returns the components defined by this record.
     *
     * @return the components defined by this record.
     */
    @NotNull List<RapidComponent> getComponents();

}
