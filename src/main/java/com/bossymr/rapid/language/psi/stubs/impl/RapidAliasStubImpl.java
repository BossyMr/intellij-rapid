package com.bossymr.rapid.language.psi.stubs.impl;

import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.bossymr.rapid.language.psi.RapidAlias;
import com.bossymr.rapid.language.psi.RapidStubElementTypes;
import com.bossymr.rapid.language.psi.stubs.RapidAliasStub;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RapidAliasStubImpl extends StubBase<RapidAlias> implements RapidAliasStub {

    private final String name, type;
    private final boolean isLocal;

    public RapidAliasStubImpl(@Nullable StubElement<?> parent, @Nullable String name, @Nullable String type, boolean isLocal) {
        super(parent, RapidStubElementTypes.ALIAS);
        this.name = name;
        this.type = type;
        this.isLocal = isLocal;
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
    public boolean isLocal() {
        return isLocal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RapidAliasStubImpl that = (RapidAliasStubImpl) o;
        return isLocal() == that.isLocal() && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getType(), isLocal());
    }

    @Override
    public String toString() {
        return "RapidAliasStub{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", isLocal=" + isLocal +
                '}';
    }
}
