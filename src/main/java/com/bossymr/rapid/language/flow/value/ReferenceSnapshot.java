package com.bossymr.rapid.language.flow.value;

import org.jetbrains.annotations.Nullable;

/**
 * A snapshot represents the state of a variable at a specific moment. As variables are mutable, previous constraints
 * and conditions would be lost if a variable is reassigned. However, with constraints, conditions and constraints can
 * reference a snapshot of the variable instead.
 */
public non-sealed interface ReferenceSnapshot extends ReferenceValue {

    /**
     * Returns the actual variable of the snapshot.
     *
     * @return the actual variable of the snapshot.
     */
    @Nullable ReferenceValue getVariable();

}
