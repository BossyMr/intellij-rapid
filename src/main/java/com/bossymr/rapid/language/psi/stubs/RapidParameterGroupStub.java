package com.bossymr.rapid.language.psi.stubs;

import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidParameterGroup;

public interface RapidParameterGroupStub extends StubElement<RapidParameterGroup> {

    /**
     * Checks if the parameter group is optional.
     *
     * @return if the parameter group is optional
     */
    boolean isOptional();

}
