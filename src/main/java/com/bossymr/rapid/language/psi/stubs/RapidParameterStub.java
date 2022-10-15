package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.bossymr.rapid.language.psi.RapidParameter;
import com.bossymr.rapid.language.psi.RapidParameter.Attribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a field stub.
 */
public interface RapidParameterStub extends NamedStub<RapidParameter> {

    /**
     * Returns the attribute of this parameter stub.
     *
     * @return the attribute of this parameter stub.
     */
    @NotNull Attribute getAttribute();

    /**
     * Returns the type of this field.
     *
     * @return the type of this field.
     */
    @Nullable String getType();
}
