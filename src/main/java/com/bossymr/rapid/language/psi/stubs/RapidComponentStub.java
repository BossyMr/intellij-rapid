package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.NamedStub;
import com.bossymr.rapid.language.psi.RapidComponent;
import org.jetbrains.annotations.Nullable;

public interface RapidComponentStub extends NamedStub<RapidComponent> {
    /**
     * Returns the type of this component.
     *
     * @return the type of this component.
     */
    @Nullable String getType();
}
