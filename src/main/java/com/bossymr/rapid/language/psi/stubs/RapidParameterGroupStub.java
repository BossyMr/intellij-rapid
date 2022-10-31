package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameterGroup;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidParameterGroupStub extends StubBase<PhysicalParameterGroup> {

    private final boolean isOptional;

    public RapidParameterGroupStub(@Nullable StubElement parent, boolean isOptional) {
        super(parent, RapidStubElementTypes.PARAMETER_GROUP);
        this.isOptional = isOptional;
    }

    public boolean isOptional() {
        return isOptional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidParameterGroupStub that = (RapidParameterGroupStub) o;
        return isOptional() == that.isOptional();
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOptional());
    }

    @Override
    public String toString() {
        return "RapidParameterGroupStub";
    }
}
