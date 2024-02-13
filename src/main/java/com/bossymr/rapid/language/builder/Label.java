package com.bossymr.rapid.language.builder;

import org.jetbrains.annotations.Nullable;

/**
 * A label which points to a specific instruction.
 */
public interface Label {

    /**
     * Returns the name of the label.
     *
     * @return the name of the label.
     */
    @Nullable String getName();

}
