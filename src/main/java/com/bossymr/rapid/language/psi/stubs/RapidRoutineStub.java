package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.bossymr.rapid.language.psi.RapidRoutine;
import com.bossymr.rapid.language.psi.RapidRoutine.Attribute;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a field stub.
 */
public interface RapidRoutineStub extends NamedStub<RapidRoutine> {

    /**
     * Returns the attribute of this routine.
     *
     * @return the attribute of this routine.
     */
    Attribute getAttribute();

    /**
     * Checks if the field is local.
     *
     * @return if the field is local.
     */
    boolean isLocal();

    /**
     * Returns the type of this field.
     *
     * @return the type of this field.
     */
    @Nullable String getType();
}
