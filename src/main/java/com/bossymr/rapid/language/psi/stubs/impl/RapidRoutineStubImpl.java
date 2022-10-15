package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidRoutine;
import com.bossymr.rapid.language.psi.RapidRoutine.Attribute;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidRoutineStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidRoutineStubImpl extends StubBase<RapidRoutine> implements RapidRoutineStub {

    private final Attribute attribute;
    private final boolean isLocal;
    private final String name, type;

    public RapidRoutineStubImpl(@Nullable StubElement<?> parent, @NotNull Attribute attribute, @Nullable String name, @Nullable String type, boolean isLocal) {
        super(parent, RapidStubElementTypes.ROUTINE);
        this.attribute = attribute;
        this.name = name;
        this.type = type;
        this.isLocal = isLocal;
    }

    @Override
    public Attribute getAttribute() {
        return attribute;
    }

    @Override
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public @Nullable String getType() {
        return name;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidRoutineStubImpl that = (RapidRoutineStubImpl) o;
        return isLocal() == that.isLocal() && getAttribute() == that.getAttribute() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttribute(), isLocal(), getName(), getType());
    }

    @Override
    public String toString() {
        return "RapidRoutineStub{" +
                "attribute=" + attribute +
                ", isLocal=" + isLocal +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
