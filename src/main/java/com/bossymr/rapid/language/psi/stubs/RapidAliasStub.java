package com.bossymr.rapid.language.psi.stubs;

import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.symbol.Visibility;
import com.bossymr.rapid.language.symbol.physical.PhysicalAlias;
import com.intellij.psi.stubs.NamedStubBase;
import com.intellij.psi.stubs.StubElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidAliasStub extends NamedStubBase<PhysicalAlias> {

    private final Visibility visibility;
    private final String type;

    public RapidAliasStub(@Nullable StubElement<?> parent, @NotNull Visibility visibility, @Nullable String name, @Nullable String type) {
        super(parent, RapidStubElementTypes.ALIAS, name);
        this.visibility = visibility;
        this.type = type;
    }

    public @NotNull Visibility getVisibility() {
        return visibility;
    }

    public @Nullable String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidAliasStub stub = (RapidAliasStub) o;
        return getVisibility() == stub.getVisibility() && Objects.equals(getName(), stub.getName()) && Objects.equals(getType(), stub.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVisibility(), getName(), getType());
    }

    @Override
    public String toString() {
        return "RapidAliasStub:" + getName();
    }
}
