package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.RapidParameter.Attribute;
import com.bossymr.rapid.language.symbol.physical.PhysicalParameter;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidParameterStub extends NamedStubBase<PhysicalParameter> {

    private final Attribute attribute;
    private final String type;
    private final int dimensions;

    public RapidParameterStub(StubElement parent, @NotNull Attribute attribute, @Nullable String name, @Nullable String type, int dimensions) {
        super(parent, RapidStubElementTypes.PARAMETER, name);
        this.attribute = attribute;
        this.type = type;
        this.dimensions = dimensions;
    }

    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    public @Nullable String getType() {
        return type;
    }

    public int getDimensions() {
        return dimensions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidParameterStub stub = (RapidParameterStub) o;
        return getDimensions() == stub.getDimensions() && getAttribute() == stub.getAttribute() && Objects.equals(getName(), stub.getName()) && Objects.equals(getType(), stub.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttribute(), getType(), getName(), getDimensions());
    }

    @Override
    public String toString() {
        return "RapidParameterStub:" + getName();
    }
}
