package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTypeStub;
import com.bossymr.rapid.language.symbol.RapidField.Attribute;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalField;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidFieldStub extends NamedStubBase<PhysicalField> implements RapidVisibleStub, RapidTypeStub {

    private final Visibility visibility;
    private final Attribute attribute;
    private final String type, initializer;
    private final int dimensions;

    public RapidFieldStub(@Nullable StubElement<?> parent, @NotNull Visibility visibility, @NotNull Attribute attribute, @Nullable String name, @Nullable String type, int dimensions, @Nullable String initializer) {
        super(parent, RapidStubElementTypes.FIELD, name);
        this.visibility = visibility;
        this.attribute = attribute;
        this.type = type;
        this.dimensions = dimensions;
        this.initializer = initializer;
    }

    public @NotNull Attribute getAttribute() {
        return attribute;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    @Override
    public @Nullable String getType() {
        return type;
    }

    @Override
    public int getDimensions() {
        return dimensions;
    }

    public @Nullable String getInitializer() {
        return initializer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidFieldStub stub = (RapidFieldStub) o;
        return getDimensions() == stub.getDimensions() && getVisibility() == stub.getVisibility() && getAttribute() == stub.getAttribute() && Objects.equals(getName(), stub.getName()) && Objects.equals(getType(), stub.getType()) && Objects.equals(getInitializer(), stub.getInitializer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getAttribute(), getName(), getType(), getInitializer(), getDimensions());
    }

    @Override
    public String toString() {
        return "RapidFieldStub:" + getName();
    }
}
