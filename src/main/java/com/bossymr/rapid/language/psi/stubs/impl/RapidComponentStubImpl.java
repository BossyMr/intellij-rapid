package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidComponent;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidComponentStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidComponentStubImpl extends StubBase<RapidComponent> implements RapidComponentStub {

    private final String name, type;

    public RapidComponentStubImpl(@Nullable StubElement<?> parent, @Nullable String name, @Nullable String type) {
        super(parent, RapidStubElementTypes.COMPONENT);
        this.name = name;
        this.type = type;
    }

    @Override
    public @Nullable String getType() {
        return type;
    }

    @Override
    public @NonNls @Nullable String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidComponentStubImpl that = (RapidComponentStubImpl) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType());
    }

    @Override
    public String toString() {
        return "RapidComponentStub{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
