package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.RapidTypeStub;
import com.bossymr.rapid.language.symbol.RoutineType;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalRoutine;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRoutineStub extends NamedStubBase<PhysicalRoutine> implements RapidVisibleStub, RapidTypeStub {

    private final Visibility visibility;
    private final RoutineType attribute;
    private final String type;

    public RapidRoutineStub(@Nullable StubElement<?> parent, @NotNull Visibility visibility, @NotNull RoutineType attribute, @Nullable String name, @Nullable String type) {
        super(parent, RapidStubElementTypes.ROUTINE, name);
        this.visibility = visibility;
        this.attribute = attribute;
        this.type = type;
    }

    @Override
    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    public RoutineType getAttribute() {
        return attribute;
    }

    @Override
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
