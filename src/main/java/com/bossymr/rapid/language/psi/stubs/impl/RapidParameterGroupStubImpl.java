package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidParameterGroup;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidParameterGroupStub;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidParameterGroupStubImpl extends StubBase<RapidParameterGroup> implements RapidParameterGroupStub {

    private final boolean isOptional;

    public RapidParameterGroupStubImpl(@Nullable StubElement<?> parent, boolean isOptional) {
        super(parent, RapidStubElementTypes.PARAMETER_GROUP);
        this.isOptional = isOptional;
    }

    @Override
    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidParameterGroupStubImpl that = (RapidParameterGroupStubImpl) o;
        return isOptional() == that.isOptional();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOptional());
    }

    @Override
    public String toString() {
        return "RapidParameterGroupStub{" +
                "isOptional=" + isOptional +
                '}';
    }
}
