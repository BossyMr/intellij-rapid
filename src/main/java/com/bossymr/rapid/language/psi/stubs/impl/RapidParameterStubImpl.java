package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidParameter;
import com.bossymr.rapid.language.psi.RapidParameter.Attribute;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidParameterStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidParameterStubImpl extends StubBase<RapidParameter> implements RapidParameterStub {

    private final Attribute attribute;
    private final String name, type;

    public RapidParameterStubImpl(@Nullable StubElement<?> parent, @NotNull Attribute attribute, @NotNull String name, @NotNull String type) {
        super(parent, RapidStubElementTypes.PARAMETER);
        this.attribute = attribute;
        this.name = name;
        this.type = type;
    }

    @Override
    public @NotNull Attribute getAttribute() {
        return attribute;
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
        RapidParameterStubImpl that = (RapidParameterStubImpl) o;
        return getAttribute() == that.getAttribute() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAttribute(), getName(), getType());
    }

    @Override
    public String toString() {
        return "RapidParameterStub{" +
                "attribute=" + attribute +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
