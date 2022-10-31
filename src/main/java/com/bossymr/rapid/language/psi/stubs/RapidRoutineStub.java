package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.RapidRoutine.Attribute;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRoutineStub extends NamedStubBase<PhysicalRoutine> {

    private final Visibility visibility;
    private final Attribute attribute;
    private final String type;

    public RapidRoutineStub(@Nullable StubElement<?> parent, @NotNull Visibility visibility, @NotNull Attribute attribute, @Nullable String name, @Nullable String type) {
        super(parent, RapidStubElementTypes.ROUTINE, name);
        this.visibility = visibility;
        this.attribute = attribute;
        this.type = type;
    }

    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidRoutineStub that = (RapidRoutineStub) o;
        return getVisibility() == that.getVisibility() && getAttribute() == that.getAttribute() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getAttribute(), getName(), getType());
    }

    @Override
    public String toString() {
        return "RapidRoutineStub:" + getName();
    }
}
